package it.sectec.platform.inventory.entity;

import it.sectec.platform.catalog.entity.TipoArticolo;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Entity
@Table(name = "bolla_tipo_articolo", uniqueConstraints = {
        @UniqueConstraint(name = "uq_bolla_tipo_articolo", columnNames = {"id_bolla", "id_tipo_articolo"})
})
public class BollaTipoArticolo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_bolla", nullable = false)
    private Bolla bolla;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_articolo", nullable = false)
    private TipoArticolo tipoArticolo;

    @NotNull
    @Min(1)
    @Column(name = "quantita", nullable = false)
    private Integer quantita;

    protected BollaTipoArticolo() {
    }

    public BollaTipoArticolo(Bolla bolla, TipoArticolo tipoArticolo, Integer quantita) {
        this.bolla = bolla;
        this.tipoArticolo = tipoArticolo;
        this.quantita = quantita;
    }

    public UUID getId() {
        return id;
    }

    public Bolla getBolla() {
        return bolla;
    }

    public void setBolla(Bolla bolla) {
        this.bolla = bolla;
    }

    public TipoArticolo getTipoArticolo() {
        return tipoArticolo;
    }

    public void setTipoArticolo(TipoArticolo tipoArticolo) {
        this.tipoArticolo = tipoArticolo;
    }

    public Integer getQuantita() {
        return quantita;
    }

    public void setQuantita(Integer quantita) {
        this.quantita = quantita;
    }
}

