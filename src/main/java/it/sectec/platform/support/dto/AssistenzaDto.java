package it.sectec.platform.support.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;

public final class AssistenzaDto {
    private AssistenzaDto() {}
    public record Request(
            @NotNull UUID articoloId,
            @NotNull UUID tipoAssistenzaId,
            @NotNull LocalDate dataInizio,
            @NotNull LocalDate dataFine,
            String note) {}
    public record Response(UUID id,
            UUID articoloId,
            UUID tipoAssistenzaId,
            LocalDate dataInizio,
            LocalDate dataFine,
            String note) {}
}

