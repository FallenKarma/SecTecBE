package it.sectec.platform.identity.controller;

import it.sectec.platform.identity.service.AccountLifecycleService;
import it.sectec.platform.identity.service.AccountRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AccountLifecycleController {
    private final AccountLifecycleService service;
    private final AccountRateLimiter limiter;
    public AccountLifecycleController(AccountLifecycleService service, AccountRateLimiter limiter) {
        this.service = service; this.limiter = limiter;
    }
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody Registration input, HttpServletRequest request) {
        limiter.check(request.getRemoteAddr());
        service.register(input.email(), input.password(), input.nome(), input.cognome());
        return accepted();
    }
        @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resend(@Valid @RequestBody EmailInput input, HttpServletRequest request) {
        limiter.check(request.getRemoteAddr());
        service.resendVerification(input.email());
        return accepted();
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgot(@Valid @RequestBody EmailInput input, HttpServletRequest request) {
        limiter.check(request.getRemoteAddr());
        service.forgotPassword(input.email());
        return accepted();
    }
    @PostMapping("/verify-email")
    public ResponseEntity<Void> verify(@Valid @RequestBody TokenInput input, HttpServletRequest request) {
        limiter.check(request.getRemoteAddr());
        service.verifyEmail(input.token());
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/reset-password")
    public ResponseEntity<Void> reset(@Valid @RequestBody ResetInput input, HttpServletRequest request) {
        limiter.check(request.getRemoteAddr());
        service.resetPassword(input.token(), input.password());
        return ResponseEntity.noContent().build();
    }
    private ResponseEntity<Map<String, String>> accepted() {
        return ResponseEntity.accepted().body(Map.of("code", "REQUEST_ACCEPTED"));
    }
    public record Registration(@NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 12, max = 72) String password,
            @NotBlank @Size(max = 100) String nome, @NotBlank @Size(max = 100) String cognome) {
        @Override public String toString() { return "Registration[redacted]"; }
    }
    public record EmailInput(@NotBlank @Email @Size(max = 254) String email) {}
    public record TokenInput(@NotBlank @Size(max = 128) String token) {
        @Override public String toString() { return "TokenInput[redacted]"; }
    }
    public record ResetInput(@NotBlank @Size(max = 128) String token,
            @NotBlank @Size(min = 12, max = 72) String password) {
        @Override public String toString() { return "ResetInput[redacted]"; }
    }
}

