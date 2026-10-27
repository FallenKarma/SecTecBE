package it.sectec.platform.identity.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;

public final class UserRoleDto {
    private UserRoleDto() {}
    public record Request(
            @NotNull UUID userId,
            @NotNull UUID roleId,
            @NotNull Instant assignedAt) {}
    public record Response(UUID id,
            UUID userId,
            UUID roleId,
            Instant assignedAt) {}
}

