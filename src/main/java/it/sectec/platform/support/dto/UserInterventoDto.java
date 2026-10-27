package it.sectec.platform.support.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;

public final class UserInterventoDto {
    private UserInterventoDto() {}
    public record Request(
            @NotNull UUID interventoId,
            @NotNull UUID userId) {}
    public record Response(UUID id,
            UUID interventoId,
            UUID userId) {}
}

