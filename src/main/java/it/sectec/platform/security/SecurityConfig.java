package it.sectec.platform.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.*;
import org.springframework.security.web.authentication.session.*;
import org.springframework.security.web.csrf.*;
import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(DatabaseUserDetailsService users, PasswordEncoder encoder) {
        var provider = new DaoAuthenticationProvider(users);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    CsrfTokenRepository csrfTokenRepository() {
        return new HttpSessionCsrfTokenRepository();
    }

    @Bean
    SessionAuthenticationStrategy sessionAuthenticationStrategy(CsrfTokenRepository csrf) {
        return new CompositeSessionAuthenticationStrategy(List.of(
                new ChangeSessionIdAuthenticationStrategy(), new CsrfAuthenticationStrategy(csrf)));
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository contexts,
                                           CsrfTokenRepository csrf, it.sectec.platform.identity.repository.UserRepository users) throws Exception {
        return http
                .addFilterAfter(new CredentialsVersionFilter(users), SecurityContextHolderFilter.class)
                .securityContext(context -> context.securityContextRepository(contexts))
                .csrf(config -> config.csrfTokenRepository(csrf))
                .requestCache(cache -> cache.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/actuator/health", "/api/v1/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/resend-verification",
                                "/api/v1/auth/verify-email", "/api/v1/auth/forgot-password", "/api/v1/auth/reset-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/me").authenticated()
                        .requestMatchers(
                                "/api/v1/users", "/api/v1/users/*",
                                "/api/v1/roles", "/api/v1/roles/*",
                                "/api/v1/permissions", "/api/v1/permissions/*",
                                "/api/v1/user-roles", "/api/v1/user-roles/*",
                                "/api/v1/role-permissions", "/api/v1/role-permissions/*",
                                "/api/v1/categorie", "/api/v1/categorie/*",
                                "/api/v1/tipi-articolo", "/api/v1/tipi-articolo/*",
                                "/api/v1/file-assets", "/api/v1/file-assets/*",
                                "/api/v1/immagini-articolo", "/api/v1/immagini-articolo/*",
                                "/api/v1/documenti-articolo", "/api/v1/documenti-articolo/*",
                                "/api/v1/tipi-intervento", "/api/v1/tipi-intervento/*",
                                "/api/v1/tipi-impianto", "/api/v1/tipi-impianto/*",
                                "/api/v1/tipi-assistenza", "/api/v1/tipi-assistenza/*",
                                "/api/v1/fornitori", "/api/v1/fornitori/*",
                                "/api/v1/clienti", "/api/v1/clienti/*",
                                "/api/v1/impianti", "/api/v1/impianti/*",
                                "/api/v1/clienti-impianti", "/api/v1/clienti-impianti/*",
                                "/api/v1/articoli", "/api/v1/articoli/*",
                                "/api/v1/assistenze", "/api/v1/assistenze/*",
                                "/api/v1/interventi", "/api/v1/interventi/*",
                                "/api/v1/user-interventi", "/api/v1/user-interventi/*",
                                "/api/v1/bolle", "/api/v1/bolle/*",
                                "/api/v1/bolle-tipi-articolo", "/api/v1/bolle-tipi-articolo/*").authenticated()
                        .anyRequest().denyAll())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, ex) ->
                                ApiSecurityError.write(response, 401, "UNAUTHENTICATED"))
                        .accessDeniedHandler((request, response, ex) ->
                                ApiSecurityError.write(response, 403,
                                ex instanceof CsrfException ? "INVALID_CSRF_TOKEN" : "FORBIDDEN")))
                .logout(logout -> logout.logoutUrl("/api/v1/auth/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) -> response.setStatus(204)))
                .build();
    }
}
