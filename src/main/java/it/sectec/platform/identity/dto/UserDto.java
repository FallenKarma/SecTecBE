package it.sectec.platform.identity.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;

public final class UserDto {
    private UserDto() {}
    public record Request(
            @NotBlank @Size(max = 254) @Email String email,
            @Size(min = 12, max = 72) String password,
            @NotBlank @Size(max = 32) @Pattern(regexp = "ACTIVE|DISABLED|LOCKED|PENDING_VERIFICATION") String status,
            Instant emailVerifiedAt,
            @NotBlank @Size(max = 100) String nome,
            @NotBlank @Size(max = 100) String cognome) {
        @Override public String toString() { return "UserRequest[credentials redacted]"; }
    }
    public record Response(UUID id,
            String email,
            String status,
            Instant emailVerifiedAt,
            String nome,
            String cognome) {}
}

