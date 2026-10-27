package it.sectec.platform.inventory.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;

public final class ArticoloDto {
    private ArticoloDto() {}
    public record Request(
            @NotNull UUID tipoArticoloId,
            UUID impiantoId,
            @Size(max = 100) String matricola,
            @NotBlank @Size(max = 32) String stato) {}
    public record Response(UUID id,
            UUID tipoArticoloId,
            UUID impiantoId,
            String matricola,
            String stato) {}
}

