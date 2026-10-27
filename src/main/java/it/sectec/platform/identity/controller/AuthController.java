package it.sectec.platform.identity.controller;

import it.sectec.platform.security.AccountPrincipal;
import it.sectec.platform.security.ApiSecurityError;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository contexts;
    private final SessionAuthenticationStrategy sessions;

    public AuthController(AuthenticationManager authenticationManager,
                          SecurityContextRepository contexts, SessionAuthenticationStrategy sessions) {
        this.authenticationManager = authenticationManager;
        this.contexts = contexts;
        this.sessions = sessions;
    }

    @GetMapping("/csrf")
    public CsrfResponse csrf(CsrfToken token) {
        return new CsrfResponse(token.getHeaderName(), token.getToken());
    }

    @PostMapping("/login")
    public ResponseEntity<AccountPrincipal.Profile> login(@Valid @RequestBody LoginRequest input,
                                                         HttpServletRequest request,
                                                         HttpServletResponse response) throws IOException {
        // A failed re-authentication must not leave an earlier identity logged in.
        var empty = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(empty);
        contexts.saveContext(empty, request, response);
        try {
            log.debug("Login authentication started");
            var authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(input.email(), input.password()));
            sessions.onAuthentication(authentication, request, response);
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            contexts.saveContext(context, request, response);
            log.info("Login succeeded; authenticated session established");
            return ResponseEntity.ok(((AccountPrincipal) authentication.getPrincipal()).profile());
        } catch (AuthenticationException ex) {
            log.warn("Login rejected: reason={}", ex.getClass().getSimpleName());
            SecurityContextHolder.clearContext();
            ApiSecurityError.write(response, 401, "INVALID_CREDENTIALS");
            return null;
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public AccountPrincipal.Profile me(@AuthenticationPrincipal AccountPrincipal principal) {
        return principal.profile();
    }

    public record LoginRequest(@NotBlank @Size(max = 254) String email,
                               @NotBlank @Size(max = 1024) String password) {}
    public record CsrfResponse(String headerName, String token) {}
}

