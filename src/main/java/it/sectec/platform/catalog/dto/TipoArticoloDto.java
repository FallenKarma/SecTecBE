package it.sectec.platform.catalog.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;

public final class TipoArticoloDto {
    private TipoArticoloDto() {}
    public record Request(
            @NotNull UUID categoriaId,
            @NotBlank @Size(max = 64) String codiceArticolo,
            @NotBlank String descrizione,
            @NotBlank @Size(max = 100) String marca,
            @NotBlank @Size(max = 100) String modello,
            @DecimalMin("0") @Digits(integer = 10, fraction = 2) BigDecimal costo) {}
    public record Response(UUID id,
            UUID categoriaId,
            String codiceArticolo,
            String descrizione,
            String marca,
            String modello,
            BigDecimal costo) {}
}

