package it.sectec.platform.installation.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;

public final class ImpiantoDto {
    private ImpiantoDto() {}
    public record Request(
            @NotNull UUID tipoImpiantoId,
            @NotBlank @Size(max = 255) String indirizzo,
            @NotBlank @Size(max = 16) String cap,
            @NotBlank @Size(max = 150) String nome,
            String descrizione,
            String numeroSim,
            String codiceSim,
            @Digits(integer = 3, fraction = 6) BigDecimal longitudine,
            @Digits(integer = 3, fraction = 6) BigDecimal latitudine,
            @NotBlank @Size(max = 255) String comune) {}
    public record Response(UUID id,
            UUID tipoImpiantoId,
            String indirizzo,
            String cap,
            String nome,
            String descrizione,
            String numeroSim,
            String codiceSim,
            BigDecimal longitudine,
            BigDecimal latitudine,
            String comune) {}
}

