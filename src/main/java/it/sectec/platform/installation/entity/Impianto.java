package it.sectec.platform.installation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "impianto")
public class Impianto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_impianto", nullable = false)
    private TipoImpianto tipoImpianto;

    @NotBlank
    @Size(max = 255)
    @Column(name = "indirizzo", nullable = false, length = 255)
    private String indirizzo;

    @NotBlank
    @Size(max = 16)
    @Column(name = "cap", nullable = false, length = 16)
    private String cap;

    @NotBlank
    @Size(max = 150)
    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "descrizione", nullable = true, columnDefinition = "text")
    private String descrizione;

    @Column(name = "numero_sim", nullable = true, columnDefinition = "text")
    private String numeroSim;

    @Column(name = "codice_sim", nullable = true, columnDefinition = "text")
    private String codiceSim;

    @Digits(integer = 3, fraction = 6)
    @Column(name = "longitudine", nullable = true, precision = 9, scale = 6)
    private BigDecimal longitudine;

    @Digits(integer = 3, fraction = 6)
    @Column(name = "latitudine", nullable = true, precision = 9, scale = 6)
    private BigDecimal latitudine;

    @NotBlank
    @Size(max = 255)
    @Column(name = "comune", nullable = false, length = 255)
    private String comune;

    protected Impianto() {
    }

    public Impianto(TipoImpianto tipoImpianto, String indirizzo, String cap, String nome, String descrizione, String numeroSim, String codiceSim, BigDecimal longitudine, BigDecimal latitudine, String comune) {
        this.tipoImpianto = tipoImpianto;
        this.indirizzo = indirizzo;
        this.cap = cap;
        this.nome = nome;
        this.descrizione = descrizione;
        this.numeroSim = numeroSim;
        this.codiceSim = codiceSim;
        this.longitudine = longitudine;
        this.latitudine = latitudine;
        this.comune = comune;
    }

    public UUID getId() {
        return id;
    }

    public TipoImpianto getTipoImpianto() {
        return tipoImpianto;
    }

    public void setTipoImpianto(TipoImpianto tipoImpianto) {
        this.tipoImpianto = tipoImpianto;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public String getCap() {
        return cap;
    }

    public void setCap(String cap) {
        this.cap = cap;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getNumeroSim() {
        return numeroSim;
    }

    public void setNumeroSim(String numeroSim) {
        this.numeroSim = numeroSim;
    }

    public String getCodiceSim() {
        return codiceSim;
    }

    public void setCodiceSim(String codiceSim) {
        this.codiceSim = codiceSim;
    }

    public BigDecimal getLongitudine() {
        return longitudine;
    }

    public void setLongitudine(BigDecimal longitudine) {
        this.longitudine = longitudine;
    }

    public BigDecimal getLatitudine() {
        return latitudine;
    }

    public void setLatitudine(BigDecimal latitudine) {
        this.latitudine = latitudine;
    }

    public String getComune() {
        return comune;
    }

    public void setComune(String comune) {
        this.comune = comune;
    }
}

