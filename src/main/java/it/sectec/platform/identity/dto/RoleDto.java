package it.sectec.platform.identity.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;

public final class RoleDto {
    private RoleDto() {}
    public record Request(
            @NotBlank @Size(max = 64) String name,
            String description) {}
    public record Response(UUID id,
            String name,
            String description) {}
}

