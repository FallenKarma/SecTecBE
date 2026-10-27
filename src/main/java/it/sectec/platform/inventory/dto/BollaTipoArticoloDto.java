package it.sectec.platform.inventory.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;

public final class BollaTipoArticoloDto {
    private BollaTipoArticoloDto() {}
    public record Request(
            @NotNull UUID bollaId,
            @NotNull UUID tipoArticoloId,
            @NotNull @Min(1) Integer quantita) {}
    public record Response(UUID id,
            UUID bollaId,
            UUID tipoArticoloId,
            Integer quantita) {}
}

