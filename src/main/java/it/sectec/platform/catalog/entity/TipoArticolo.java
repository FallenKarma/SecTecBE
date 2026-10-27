package it.sectec.platform.catalog.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tipo_articolo")
public class TipoArticolo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @NotBlank
    @Size(max = 64)
    @Column(name = "codice_articolo", nullable = false, length = 64, unique = true)
    private String codiceArticolo;

    @NotBlank
    @Column(name = "descrizione", nullable = false, columnDefinition = "text")
    private String descrizione;

    @NotBlank
    @Size(max = 100)
    @Column(name = "marca", nullable = false, length = 100)
    private String marca;

    @NotBlank
    @Size(max = 100)
    @Column(name = "modello", nullable = false, length = 100)
    private String modello;

    @DecimalMin("0")
    @Digits(integer = 10, fraction = 2)
    @Column(name = "costo", nullable = true, precision = 12, scale = 2)
    private BigDecimal costo;

    protected TipoArticolo() {
    }

    public TipoArticolo(Categoria categoria, String codiceArticolo, String descrizione, String marca, String modello, BigDecimal costo) {
        this.categoria = categoria;
        this.codiceArticolo = codiceArticolo;
        this.descrizione = descrizione;
        this.marca = marca;
        this.modello = modello;
        this.costo = costo;
    }

    public UUID getId() {
        return id;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getCodiceArticolo() {
        return codiceArticolo;
    }

    public void setCodiceArticolo(String codiceArticolo) {
        this.codiceArticolo = codiceArticolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModello() {
        return modello;
    }

    public void setModello(String modello) {
        this.modello = modello;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }
}

