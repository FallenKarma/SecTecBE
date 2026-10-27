package it.sectec.platform.storage.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;

public final class FileAssetDto {
    private FileAssetDto() {}
    public record Request(
            @NotBlank @Size(max = 16) @Pattern(regexp = "LOCAL|S3") String storageBackend,
            @NotBlank @Size(max = 128) String storageNamespace,
            @NotBlank String storageKey,
            @NotBlank @Size(max = 255) String originalName,
            @NotBlank @Size(max = 127) String mimeType,
            @NotNull @Min(0) Long byteSize,
            @NotBlank @Size(max = 64) @Pattern(regexp = "[0-9a-fA-F]{64}") String sha256,
            @NotBlank @Size(max = 16) @Pattern(regexp = "PUBLIC|PRIVATE") String visibility,
            @NotNull Instant createdAt) {}
    public record Response(UUID id,
            String storageBackend,
            String storageNamespace,
            String storageKey,
            String originalName,
            String mimeType,
            Long byteSize,
            String sha256,
            String visibility,
            Instant createdAt) {}
}

