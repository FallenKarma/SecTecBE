package it.sectec.platform.catalog.entity;

import it.sectec.platform.storage.entity.FileAsset;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Entity
@Table(name = "documento_articolo", uniqueConstraints = {
        @UniqueConstraint(name = "uq_documento_articolo", columnNames = {"id_tipo_articolo", "file_asset_id", "version"})
})
public class DocumentoArticolo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_asset_id", nullable = false)
    private FileAsset fileAsset;

    @NotBlank
    @Size(max = 50)
    @Column(name = "version", nullable = false, length = 50)
    private String version;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_articolo", nullable = false)
    private TipoArticolo tipoArticolo;

    protected DocumentoArticolo() {
    }

    public DocumentoArticolo(FileAsset fileAsset, String version, TipoArticolo tipoArticolo) {
        this.fileAsset = fileAsset;
        this.version = version;
        this.tipoArticolo = tipoArticolo;
    }

    public UUID getId() {
        return id;
    }

    public FileAsset getFileAsset() {
        return fileAsset;
    }

    public void setFileAsset(FileAsset fileAsset) {
        this.fileAsset = fileAsset;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public TipoArticolo getTipoArticolo() {
        return tipoArticolo;
    }

    public void setTipoArticolo(TipoArticolo tipoArticolo) {
        this.tipoArticolo = tipoArticolo;
    }
}

