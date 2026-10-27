package it.sectec.platform.identity.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;

public final class RolePermissionDto {
    private RolePermissionDto() {}
    public record Request(
            @NotNull UUID roleId,
            @NotNull UUID permissionId) {}
    public record Response(UUID id,
            UUID roleId,
            UUID permissionId) {}
}

