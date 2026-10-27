package it.sectec.platform.support.entity;

import it.sectec.platform.inventory.entity.Articolo;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "assistenza")
public class Assistenza {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_articolo", nullable = false)
    private Articolo articolo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_assistenza", nullable = false)
    private TipoAssistenza tipoAssistenza;

    @NotNull
    @Column(name = "data_inizio", nullable = false)
    private LocalDate dataInizio;

    @NotNull
    @Column(name = "data_fine", nullable = false)
    private LocalDate dataFine;

    @Column(name = "note", nullable = true, columnDefinition = "text")
    private String note;

    protected Assistenza() {
    }

    public Assistenza(Articolo articolo, TipoAssistenza tipoAssistenza, LocalDate dataInizio, LocalDate dataFine, String note) {
        this.articolo = articolo;
        this.tipoAssistenza = tipoAssistenza;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.note = note;
    }

    public UUID getId() {
        return id;
    }

    public Articolo getArticolo() {
        return articolo;
    }

    public void setArticolo(Articolo articolo) {
        this.articolo = articolo;
    }

    public TipoAssistenza getTipoAssistenza() {
        return tipoAssistenza;
    }

    public void setTipoAssistenza(TipoAssistenza tipoAssistenza) {
        this.tipoAssistenza = tipoAssistenza;
    }

    public LocalDate getDataInizio() {
        return dataInizio;
    }

    public void setDataInizio(LocalDate dataInizio) {
        this.dataInizio = dataInizio;
    }

    public LocalDate getDataFine() {
        return dataFine;
    }

    public void setDataFine(LocalDate dataFine) {
        this.dataFine = dataFine;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

