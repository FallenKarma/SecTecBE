package it.sectec.platform.support.entity;

import it.sectec.platform.installation.entity.Impianto;
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
    @JoinColumn(name = "id_impianto", nullable = false)
    private Impianto impianto;

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

    public Assistenza(Impianto impianto, TipoAssistenza tipoAssistenza, LocalDate dataInizio, LocalDate dataFine, String note) {
        this.impianto = impianto;
        this.tipoAssistenza = tipoAssistenza;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.note = note;
    }

    public UUID getId() {
        return id;
    }

    public Impianto getImpianto() {
        return impianto;
    }

    public void setImpianto(Impianto impianto) {
        this.impianto = impianto;
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

