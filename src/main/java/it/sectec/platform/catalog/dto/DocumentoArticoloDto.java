package it.sectec.platform.catalog.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;

public final class DocumentoArticoloDto {
    private DocumentoArticoloDto() {}
    public record Request(
            @NotNull UUID fileAssetId,
            @NotBlank @Size(max = 50) String version,
            @NotNull UUID tipoArticoloId) {}
    public record Response(UUID id,
            UUID fileAssetId,
            String version,
            UUID tipoArticoloId) {}
}

