package it.sectec.platform.support.entity;

import it.sectec.platform.installation.entity.Impianto;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "intervento")
public class Intervento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_impianto", nullable = false)
    private Impianto impianto;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_intervento", nullable = false)
    private TipoIntervento tipoIntervento;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_assistenza", nullable = true)
    private Assistenza assistenza;

    @NotNull
    @Column(name = "data", nullable = false)
    private Instant data;

    @Column(name = "note", nullable = true, columnDefinition = "text")
    private String note;

    @Column(name = "esito", nullable = true, columnDefinition = "text")
    private String esito;

    protected Intervento() {
    }

    public Intervento(Impianto impianto, TipoIntervento tipoIntervento, Assistenza assistenza, Instant data, String note, String esito) {
        this.impianto = impianto;
        this.tipoIntervento = tipoIntervento;
        this.assistenza = assistenza;
        this.data = data;
        this.note = note;
        this.esito = esito;
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

    public TipoIntervento getTipoIntervento() {
        return tipoIntervento;
    }

    public void setTipoIntervento(TipoIntervento tipoIntervento) {
        this.tipoIntervento = tipoIntervento;
    }

    public Assistenza getAssistenza() {
        return assistenza;
    }

    public void setAssistenza(Assistenza assistenza) {
        this.assistenza = assistenza;
    }

    public Instant getData() {
        return data;
    }

    public void setData(Instant data) {
        this.data = data;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getEsito() {
        return esito;
    }

    public void setEsito(String esito) {
        this.esito = esito;
    }
}

