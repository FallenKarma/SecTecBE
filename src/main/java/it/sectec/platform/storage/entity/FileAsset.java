package it.sectec.platform.storage.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "file_asset", uniqueConstraints = {
        @UniqueConstraint(name = "uq_file_asset", columnNames = {"storage_backend", "storage_namespace", "storage_key"})
})
public class FileAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(max = 16)
    @Pattern(regexp = "LOCAL|S3")
    @Column(name = "storage_backend", nullable = false, length = 16)
    private String storageBackend;

    @NotBlank
    @Size(max = 128)
    @Column(name = "storage_namespace", nullable = false, length = 128)
    private String storageNamespace;

    @NotBlank
    @Column(name = "storage_key", nullable = false, columnDefinition = "text")
    private String storageKey;

    @NotBlank
    @Size(max = 255)
    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @NotBlank
    @Size(max = 127)
    @Column(name = "mime_type", nullable = false, length = 127)
    private String mimeType;

    @NotNull
    @Min(0)
    @Column(name = "byte_size", nullable = false)
    private Long byteSize;

    @NotBlank
    @Size(max = 64)
    @Pattern(regexp = "[0-9a-fA-F]{64}")
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.CHAR)
    @Column(name = "sha256", nullable = false, length = 64, columnDefinition = "char(64)")
    private String sha256;

    @NotBlank
    @Size(max = 16)
    @Pattern(regexp = "PUBLIC|PRIVATE")
    @Column(name = "visibility", nullable = false, length = 16)
    private String visibility;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected FileAsset() {
    }

    public FileAsset(String storageBackend, String storageNamespace, String storageKey, String originalName, String mimeType, Long byteSize, String sha256, String visibility, Instant createdAt) {
        this.storageBackend = storageBackend;
        this.storageNamespace = storageNamespace;
        this.storageKey = storageKey;
        this.originalName = originalName;
        this.mimeType = mimeType;
        this.byteSize = byteSize;
        this.sha256 = sha256;
        this.visibility = visibility;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getStorageBackend() {
        return storageBackend;
    }

    public void setStorageBackend(String storageBackend) {
        this.storageBackend = storageBackend;
    }

    public String getStorageNamespace() {
        return storageNamespace;
    }

    public void setStorageNamespace(String storageNamespace) {
        this.storageNamespace = storageNamespace;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getByteSize() {
        return byteSize;
    }

    public void setByteSize(Long byteSize) {
        this.byteSize = byteSize;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

