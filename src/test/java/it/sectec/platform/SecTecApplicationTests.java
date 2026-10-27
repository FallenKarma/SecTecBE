package it.sectec.platform;

import org.junit.jupiter.api.Test;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import it.sectec.platform.identity.entity.*;
import it.sectec.platform.identity.repository.*;
import jakarta.persistence.EntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.http.MediaType;
import com.jayway.jsonpath.JsonPath;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Import(SecTecApplicationTests.AuthorizationTestConfiguration.class)
class SecTecApplicationTests {

	@Container
	@ServiceConnection
	static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17.11");

	@Autowired
	MockMvc mvc;

	@Autowired
	Flyway flyway;

	@Autowired UserRepository users;
	@Autowired RoleRepository roles;
	@Autowired PermissionRepository permissions;
	@Autowired UserRoleRepository userRoles;
	@Autowired RolePermissionRepository rolePermissions;
	@Autowired EntityManager entityManager;
	@Autowired JdbcTemplate jdbc;
	@Autowired PasswordEncoder encoder;
	@Autowired PermissionProbe permissionProbe;

	@TestConfiguration(proxyBeanMethods = false)
	static class AuthorizationTestConfiguration {
		@Bean PermissionProbe permissionProbe() { return new PermissionProbe(); }
	}

	static class PermissionProbe {
		@PreAuthorize("hasAuthority('PERM_SUPPORT_READ')")
		public String readSupport() { return "allowed"; }
		@PreAuthorize("hasAuthority('PERM_SUPPORT_WRITE')")
		public String writeSupport() { return "allowed"; }
	}

	private record CsrfSession(MockHttpSession session, String header, String token) {}

	private CsrfSession fetchCsrf(MockHttpSession session) throws Exception {
		var request = get("/api/v1/auth/csrf");
		if (session != null) request.session(session);
		var result = mvc.perform(request).andExpect(status().isOk()).andReturn();
		String body = result.getResponse().getContentAsString();
		return new CsrfSession((MockHttpSession) result.getRequest().getSession(),
				JsonPath.read(body, "$.headerName"), JsonPath.read(body, "$.token"));
	}

	private void createLoginAccount(String status) {
		var account = users.save(new User("login@example.test", encoder.encode("correct-password"),
				status, null, "Mario", "Rossi"));
		var role = roles.save(new Role("OPERATOR", null));
		var permission = permissions.save(new Permission("SUPPORT_READ", null));
		userRoles.save(new UserRole(account, role, Instant.now()));
		rolePermissions.save(new RolePermission(role, permission));
		entityManager.flush();
		entityManager.clear();
	}

	private org.springframework.test.web.servlet.ResultActions login(CsrfSession csrf, String email, String password)
			throws Exception {
		return mvc.perform(post("/api/v1/auth/login").session(csrf.session())
				.with(request -> {
					request.setRequestedSessionId(csrf.session().getId());
					request.setRequestedSessionIdValid(true);
					return request;
				})
				.header(csrf.header(), csrf.token()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
	}

	@Test
	@Transactional
	void loginRotatesSessionLoadsPermissionsAndLogoutInvalidatesIt() throws Exception {
		createLoginAccount("ACTIVE");
		var csrf = fetchCsrf(null);
		String oldSessionId = csrf.session().getId();
		login(csrf, " LOGIN@EXAMPLE.TEST ", "correct-password")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.roles[0]").value("OPERATOR"))
				.andExpect(jsonPath("$.permissions[0]").value("SUPPORT_READ"))
				.andExpect(jsonPath("$.hashedPassword").doesNotExist());
		assertThat(csrf.session().getId()).isNotEqualTo(oldSessionId);
		mvc.perform(get("/api/v1/auth/me").session(csrf.session()))
				.andExpect(status().isOk()).andExpect(jsonPath("$.email").value("login@example.test"));

		var context = (SecurityContext) csrf.session().getAttribute(
				HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
		assertThat(((org.springframework.security.core.userdetails.UserDetails)
				context.getAuthentication().getPrincipal()).getPassword()).isNull();
		SecurityContextHolder.setContext(context);
		try {
			assertThat(permissionProbe.readSupport()).isEqualTo("allowed");
			assertThatThrownBy(() -> permissionProbe.writeSupport()).isInstanceOf(AccessDeniedException.class);
		} finally {
			SecurityContextHolder.clearContext();
		}

		mvc.perform(post("/api/v1/auth/logout").session(csrf.session()).header(csrf.header(), csrf.token()))
				.andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("INVALID_CSRF_TOKEN"));
		var fresh = fetchCsrf(csrf.session());
		mvc.perform(post("/api/v1/auth/logout").session(fresh.session()).header(fresh.header(), fresh.token()))
				.andExpect(status().isNoContent()).andExpect(cookie().maxAge("JSESSIONID", 0));
		assertThat(fresh.session().isInvalid()).isTrue();
		mvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
		fetchCsrf(null);
	}

	@Test
	void loginRequiresCsrfAndMeRequiresAuthentication() throws Exception {
		mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"login@example.test\",\"password\":\"wrong\"}"))
				.andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("INVALID_CSRF_TOKEN"));
		mvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
	}

	@Test
	@Transactional
	void invalidCredentialsDoNotCreateAuthenticatedSession() throws Exception {
		createLoginAccount("ACTIVE");
		var csrf = fetchCsrf(null);
		login(csrf, "login@example.test", "wrong").andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
		login(csrf, "missing@example.test", "wrong").andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
		mvc.perform(get("/api/v1/auth/me").session(csrf.session())).andExpect(status().isUnauthorized());
	}

	@org.junit.jupiter.params.ParameterizedTest
	@org.junit.jupiter.params.provider.ValueSource(strings = {"DISABLED", "LOCKED", "UNKNOWN"})
	@Transactional
	void inactiveAccountsCannotLogin(String state) throws Exception {
		createLoginAccount(state);
		login(fetchCsrf(null), "login@example.test", "correct-password")
				.andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
	}

	@Test
	void malformedLoginReturnsSafeBadRequest() throws Exception {
		var csrf = fetchCsrf(null);
		mvc.perform(post("/api/v1/auth/login").session(csrf.session()).header(csrf.header(), csrf.token())
				.contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"do-not-echo\"}"))
				.andExpect(status().isBadRequest()).andExpect(content().json("{\"code\":\"INVALID_REQUEST\"}"));
	}

	@Test
	@Transactional
	void identityGraphCanBePersistedAndReadFromDatabase() {
		var user = users.save(new User("person@example.test", "test-hash-not-a-password", "TEST_STATUS",
				null, "Mario", "Rossi"));
		var role = roles.save(new Role("OPERATOR", "Operatore"));
		var permission = permissions.save(new Permission("SUPPORT_READ", null));
		var assignedAt = Instant.parse("2026-10-23T09:00:00Z");
		var assignment = userRoles.save(new UserRole(user, role, assignedAt));
		var grant = rolePermissions.save(new RolePermission(role, permission));
		entityManager.flush();
		entityManager.clear();

		var loaded = users.findByEmail(" PERSON@EXAMPLE.TEST ").orElseThrow();
		assertThat(loaded.getId()).isEqualTo(user.getId());
		assertThat(loaded.getEmailVerifiedAt()).isNull();
		assertThat(loaded.getNome()).isEqualTo("Mario");
		assertThat(roles.findByName("OPERATOR")).isPresent();
		assertThat(permissions.findByName("SUPPORT_READ")).isPresent();
		assertThat(userRoles.findByUser_Id(loaded.getId())).singleElement().satisfies(link -> {
			assertThat(link.getId()).isEqualTo(assignment.getId());
			assertThat(link.getAssignedAt()).isEqualTo(assignedAt);
			assertThat(link.getRole().getName()).isEqualTo("OPERATOR");
		});
		assertThat(rolePermissions.findByRole_Id(role.getId())).singleElement().satisfies(link -> {
			assertThat(link.getId()).isEqualTo(grant.getId());
			assertThat(link.getPermission().getName()).isEqualTo("SUPPORT_READ");
		});
	}

	@Test
	@Transactional
	void databaseRejectsDuplicateEmailIgnoringCaseAndSpaces() {
		insertUser("person@example.test");
		assertThatThrownBy(() -> insertUser(" PERSON@EXAMPLE.TEST "))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@Transactional
	void databaseRejectsDuplicateUserRole() {
		var userId = insertUser("person@example.test");
		var roleId = insertRole();
		jdbc.update("insert into user_role values (?, ?, ?, current_timestamp)", UUID.randomUUID(), userId, roleId);
		assertThatThrownBy(() -> jdbc.update("insert into user_role values (?, ?, ?, current_timestamp)",
				UUID.randomUUID(), userId, roleId)).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@Transactional
	void databaseRejectsDuplicateRolePermission() {
		var roleId = insertRole();
		var permissionId = UUID.randomUUID();
		jdbc.update("insert into permission values (?, 'SUPPORT_READ', null)", permissionId);
		jdbc.update("insert into role_permission values (?, ?, ?)", UUID.randomUUID(), roleId, permissionId);
		assertThatThrownBy(() -> jdbc.update("insert into role_permission values (?, ?, ?)",
				UUID.randomUUID(), roleId, permissionId)).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@Transactional
	void databasePreventsDeletionOfAssignedRole() {
		var userId = insertUser("person@example.test");
		var roleId = insertRole();
		jdbc.update("insert into user_role values (?, ?, ?, current_timestamp)", UUID.randomUUID(), userId, roleId);
		assertThatThrownBy(() -> jdbc.update("delete from role where id = ?", roleId))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@Transactional
	void databaseRejectsAssociationToMissingUser() {
		var roleId = insertRole();
		assertThatThrownBy(() -> jdbc.update("insert into user_role values (?, ?, ?, current_timestamp)",
				UUID.randomUUID(), UUID.randomUUID(), roleId)).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@Transactional
	void databaseRejectsBlankEmail() {
		assertThatThrownBy(() -> insertUser("   ")).isInstanceOf(DataIntegrityViolationException.class);
	}

	private UUID insertUser(String email) {
		var id = UUID.randomUUID();
		jdbc.update("insert into app_user (id, email, hashed_password, status, nome, cognome) values (?, ?, ?, ?, ?, ?)",
				id, email, "test-hash-not-a-password", "TEST_STATUS", "Mario", "Rossi");
		return id;
	}

	private UUID insertRole() {
		var id = UUID.randomUUID();
		jdbc.update("insert into role values (?, 'OPERATOR', null)", id);
		return id;
	}

	@Test
	void healthIsPublicAndDatabaseIsAvailable() throws Exception {
		mvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.components").doesNotExist());
		assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
	}

	@Test
	void applicationRoutesAreDeniedUntilAuthenticationIsImplemented() throws Exception {
		mvc.perform(get("/api/v1/catalog"))
				.andExpect(status().isUnauthorized());
	}

}
