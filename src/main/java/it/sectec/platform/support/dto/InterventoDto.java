package it.sectec.platform.support.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;

public final class InterventoDto {
    private InterventoDto() {}
    public record Request(
            @NotNull UUID impiantoId,
            @NotNull UUID tipoInterventoId,
            UUID assistenzaId,
            @NotNull Instant data,
            String note,
            String esito) {}
    public record Response(UUID id,
            UUID impiantoId,
            UUID tipoInterventoId,
            UUID assistenzaId,
            Instant data,
            String note,
            String esito) {}
}

