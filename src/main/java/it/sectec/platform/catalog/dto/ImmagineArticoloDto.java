package it.sectec.platform.catalog.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;

public final class ImmagineArticoloDto {
    private ImmagineArticoloDto() {}
    public record Request(
            @NotNull UUID fileAssetId,
            @NotBlank @Size(max = 255) String altText,
            @NotNull @Min(0) Integer sortOrder,
            @NotNull UUID tipoArticoloId) {}
    public record Response(UUID id,
            UUID fileAssetId,
            String altText,
            Integer sortOrder,
            UUID tipoArticoloId) {}
}

