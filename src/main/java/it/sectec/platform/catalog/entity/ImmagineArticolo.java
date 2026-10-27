package it.sectec.platform.catalog.entity;

import it.sectec.platform.storage.entity.FileAsset;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Entity
@Table(name = "immagine_articolo", uniqueConstraints = {
        @UniqueConstraint(name = "uq_immagine_articolo", columnNames = {"id_tipo_articolo", "file_asset_id"})
})
public class ImmagineArticolo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_asset_id", nullable = false)
    private FileAsset fileAsset;

    @NotBlank
    @Size(max = 255)
    @Column(name = "alt_text", nullable = false, length = 255)
    private String altText;

    @NotNull
    @Min(0)
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_articolo", nullable = false)
    private TipoArticolo tipoArticolo;

    protected ImmagineArticolo() {
    }

    public ImmagineArticolo(FileAsset fileAsset, String altText, Integer sortOrder, TipoArticolo tipoArticolo) {
        this.fileAsset = fileAsset;
        this.altText = altText;
        this.sortOrder = sortOrder;
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

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public TipoArticolo getTipoArticolo() {
        return tipoArticolo;
    }

    public void setTipoArticolo(TipoArticolo tipoArticolo) {
        this.tipoArticolo = tipoArticolo;
    }
}

