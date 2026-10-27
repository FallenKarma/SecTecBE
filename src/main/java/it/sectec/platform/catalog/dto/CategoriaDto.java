package it.sectec.platform.catalog.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;

public final class CategoriaDto {
    private CategoriaDto() {}
    public record Request(
            @NotBlank @Size(max = 100) String nome,
            String descrizione) {}
    public record Response(UUID id,
            String nome,
            String descrizione) {}
}

