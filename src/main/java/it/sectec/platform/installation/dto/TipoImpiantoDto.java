package it.sectec.platform.installation.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;

public final class TipoImpiantoDto {
    private TipoImpiantoDto() {}
    public record Request(
            @NotBlank @Size(max = 150) String nome) {}
    public record Response(UUID id,
            String nome) {}
}

