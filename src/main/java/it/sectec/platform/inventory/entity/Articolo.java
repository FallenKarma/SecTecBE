package it.sectec.platform.inventory.entity;

import it.sectec.platform.catalog.entity.TipoArticolo;
import it.sectec.platform.installation.entity.Impianto;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Entity
@Table(name = "articolo")
public class Articolo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_articolo", nullable = false)
    private TipoArticolo tipoArticolo;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_impianto", nullable = true)
    private Impianto impianto;

    @Size(max = 100)
    @Column(name = "matricola", nullable = true, length = 100)
    private String matricola;

    @NotBlank
    @Size(max = 32)
    @Column(name = "stato", nullable = false, length = 32)
    private String stato;

    protected Articolo() {
    }

    public Articolo(TipoArticolo tipoArticolo, Impianto impianto, String matricola, String stato) {
        this.tipoArticolo = tipoArticolo;
        this.impianto = impianto;
        this.matricola = matricola;
        this.stato = stato;
    }

    public UUID getId() {
        return id;
    }

    public TipoArticolo getTipoArticolo() {
        return tipoArticolo;
    }

    public void setTipoArticolo(TipoArticolo tipoArticolo) {
        this.tipoArticolo = tipoArticolo;
    }

    public Impianto getImpianto() {
        return impianto;
    }

    public void setImpianto(Impianto impianto) {
        this.impianto = impianto;
    }

    public String getMatricola() {
        return matricola;
    }

    public void setMatricola(String matricola) {
        this.matricola = matricola;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }
}

