package it.sectec.platform;

import com.jayway.jsonpath.JsonPath;
import it.sectec.platform.identity.service.AccountLifecycleService;
import it.sectec.platform.shared.crud.CrudException;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.*;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest(properties = "app.account.frontend-base-url=https://frontend.example.test")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Import(AccountLifecycleTests.TestClockConfig.class)
class AccountLifecycleTests {
    @Container @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17.11");
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder encoder;
    @Autowired MutableClock clock;
    @Autowired AccountLifecycleService service;
    @MockitoBean JavaMailSender mail;
    final BlockingQueue<SimpleMailMessage> messages = new LinkedBlockingQueue<>();
    String email;
    String address;

    @TestConfiguration(proxyBeanMethods = false)
    static class TestClockConfig {
        @Bean @Primary MutableClock testClock() { return new MutableClock(); }
    }
    static class MutableClock extends Clock {
        private volatile Instant now = Instant.parse("2026-10-25T12:00:00Z");
        void advance(Duration duration) { now = now.plus(duration); }
        public ZoneId getZone() { return ZoneOffset.UTC; }
        public Clock withZone(ZoneId zone) { return this; }
        public Instant instant() { return now; }
    }

    @BeforeEach
    void setup() {
        email = UUID.randomUUID() + "@example.test";
        address = UUID.randomUUID().toString();
        messages.clear();
        doAnswer(invocation -> { messages.add(invocation.getArgument(0)); return null; })
                .when(mail).send(any(SimpleMailMessage.class));
    }

    ResultActions postJson(String path, Map<String, ?> body) throws Exception {
        return mvc.perform(post("/api/v1/auth/" + path).with(csrf())
                .with(request -> { request.setRemoteAddr(address); return request; })
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)));
    }
    void register() throws Exception {
        postJson("register", Map.of("email", email, "password", "Initial-password-123", "nome", "Mario", "cognome", "Rossi"))
                .andExpect(status().isAccepted()).andExpect(content().json("{\"code\":\"REQUEST_ACCEPTED\"}"));
    }
    String mailToken() throws Exception {
        var message = messages.poll(5, TimeUnit.SECONDS);
        assertThat(message).isNotNull();
        assertThat(message.getTo()).containsExactly(email);
        String route = message.getSubject().contains("verifica") ? "verify-email" : "reset-password";
        assertThat(message.getText()).contains("https://frontend.example.test/#" + route + "?token=")
                .doesNotContain("/account/index.html");
        return message.getText().split("token=")[1].split("\\s")[0];
    }
    String activate() throws Exception {
        register();
        String token = mailToken();
        postJson("verify-email", Map.of("token", token)).andExpect(status().isNoContent());
        return token;
    }
    MockHttpSession login(String password) throws Exception {
        var csrfResult = mvc.perform(get("/api/v1/auth/csrf")).andExpect(status().isOk()).andReturn();
        var session = (MockHttpSession) csrfResult.getRequest().getSession();
        String token = JsonPath.read(csrfResult.getResponse().getContentAsString(), "$.token");
        mvc.perform(post("/api/v1/auth/login").session(session).header("X-CSRF-TOKEN", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk());
        return session;
    }

    @Test
    void registrationVerificationAndPasswordResetRevokeAllOldSessions() throws Exception {
        register();
        String verification = mailToken();
        String hash = jdbc.queryForObject("select token_hash from account_token where email = ?", String.class, email);
        assertThat(hash).hasSize(64).isNotEqualTo(verification);
        assertThat(jdbc.queryForObject("select status from app_user where email = ?", String.class, email)).isEqualTo("PENDING_VERIFICATION");
        postJson("login", Map.of("email", email, "password", "Initial-password-123")).andExpect(status().isUnauthorized());
        postJson("verify-email", Map.of("token", verification)).andExpect(status().isNoContent());
        postJson("verify-email", Map.of("token", verification)).andExpect(status().isBadRequest());
        var session1 = login("Initial-password-123");
        var session2 = login("Initial-password-123");
        postJson("forgot-password", Map.of("email", email)).andExpect(status().isAccepted());
        String reset = mailToken();
        postJson("reset-password", Map.of("token", reset, "password", "Replacement-password-456"))
                .andExpect(status().isNoContent());
        postJson("reset-password", Map.of("token", reset, "password", "Another-password-789"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
        mvc.perform(get("/api/v1/auth/me").session(session1)).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("SESSION_REVOKED"));
        mvc.perform(get("/api/v1/auth/me").session(session2)).andExpect(status().isUnauthorized());
        postJson("login", Map.of("email", email, "password", "Initial-password-123")).andExpect(status().isUnauthorized());
        login("Replacement-password-456");
        assertThat(jdbc.queryForObject("select count(*) from user_role ur join app_user u on u.id=ur.id_user where u.email=?", Integer.class, email)).isZero();
    }

    @Test
    void existingAndUnknownEmailsHaveSameResponseAndRegistrationDoesNotOverwrite() throws Exception {
        activate();
        String before = jdbc.queryForObject("select hashed_password from app_user where email=?", String.class, email);
        postJson("register", Map.of("email", email, "password", "Attack-password-123", "nome", "Other", "cognome", "Other"))
                .andExpect(status().isAccepted()).andExpect(content().json("{\"code\":\"REQUEST_ACCEPTED\"}"));
        assertThat(jdbc.queryForObject("select hashed_password from app_user where email=?", String.class, email)).isEqualTo(before);
        postJson("forgot-password", Map.of("email", "unknown@example.test")).andExpect(status().isAccepted())
                .andExpect(content().json("{\"code\":\"REQUEST_ACCEPTED\"}"));
        assertThat(messages).isEmpty();
    }

    @Test
    void resendInvalidatesPreviousLinkAndCooldownSuppressesRepeatedMail() throws Exception {
        register();
        String original = mailToken();
        postJson("resend-verification", Map.of("email", email)).andExpect(status().isAccepted());
        assertThat(messages).isEmpty();
        clock.advance(Duration.ofSeconds(61));
        postJson("resend-verification", Map.of("email", email)).andExpect(status().isAccepted());
        String replacement = mailToken();
        postJson("verify-email", Map.of("token", original)).andExpect(status().isBadRequest());
        postJson("verify-email", Map.of("token", replacement)).andExpect(status().isNoContent());
    }

    @Test
    void tokensExpireAndCannotBeUsedForAnotherPurpose() throws Exception {
        register();
        String token = mailToken();
        postJson("reset-password", Map.of("token", token, "password", "Replacement-password-456"))
                .andExpect(status().isBadRequest());
        clock.advance(Duration.ofHours(25));
        postJson("verify-email", Map.of("token", token)).andExpect(status().isBadRequest());
        postJson("resend-verification", Map.of("email", email)).andExpect(status().isAccepted());
        postJson("verify-email", Map.of("token", mailToken())).andExpect(status().isNoContent());
        postJson("forgot-password", Map.of("email", email)).andExpect(status().isAccepted());
        String reset = mailToken();
        clock.advance(Duration.ofMinutes(31));
        postJson("reset-password", Map.of("token", reset, "password", "Replacement-password-456"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void concurrentRedemptionAllowsOnlyOnePasswordChange() throws Exception {
        activate();
        postJson("forgot-password", Map.of("email", email)).andExpect(status().isAccepted());
        String token = mailToken();
        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<Boolean> reset = () -> {
                try { service.resetPassword(token, "Replacement-password-456"); return true; }
                catch (CrudException ex) { assertThat(ex.getMessage()).isEqualTo("INVALID_TOKEN"); return false; }
            };
            var results = executor.invokeAll(List.of(reset, reset));
            assertThat(List.of(results.get(0).get(), results.get(1).get())).containsExactlyInAnyOrder(true, false);
        }
    }

    @Test
    void publicAccountEndpointsStillRequireCsrfAndEnforceRateLimit() throws Exception {
        mvc.perform(post("/api/v1/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"unknown@example.test\"}"))
                .andExpect(status().isForbidden());
        for (int i=0; i<10; i++) postJson("forgot-password", Map.of("email", email)).andExpect(status().isAccepted());
        postJson("forgot-password", Map.of("email", email)).andExpect(status().isTooManyRequests());
        clock.advance(Duration.ofSeconds(61));
        postJson("forgot-password", Map.of("email", email)).andExpect(status().isAccepted());
    }

    @Test
    void disabledAccountCannotBeRecoveredOrReactivated() throws Exception {
        activate();
        postJson("forgot-password", Map.of("email", email)).andExpect(status().isAccepted());
        String token = mailToken();
        jdbc.update("update app_user set status='DISABLED' where email=?", email);
        postJson("reset-password", Map.of("token", token, "password", "Replacement-password-456"))
                .andExpect(status().isBadRequest());
        clock.advance(Duration.ofSeconds(61));
        postJson("forgot-password", Map.of("email", email)).andExpect(status().isAccepted());
        postJson("resend-verification", Map.of("email", email)).andExpect(status().isAccepted());
        assertThat(messages).isEmpty();
    }
}
