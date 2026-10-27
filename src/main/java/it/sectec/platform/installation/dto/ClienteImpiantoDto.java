package it.sectec.platform.installation.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;

public final class ClienteImpiantoDto {
    private ClienteImpiantoDto() {}
    public record Request(
            @NotNull UUID clienteId,
            @NotNull UUID impiantoId) {}
    public record Response(UUID id,
            UUID clienteId,
            UUID impiantoId) {}
}

