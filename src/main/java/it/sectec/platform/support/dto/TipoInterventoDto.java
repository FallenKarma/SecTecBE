package it.sectec.platform.support.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;

public final class TipoInterventoDto {
    private TipoInterventoDto() {}
    public record Request(
            @NotBlank @Size(max = 150) String nome) {}
    public record Response(UUID id,
            String nome) {}
}

