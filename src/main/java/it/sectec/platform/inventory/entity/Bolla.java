package it.sectec.platform.inventory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "bolla")
public class Bolla {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_fornitore", nullable = true)
    private Fornitore fornitore;

    @NotBlank
    @Size(max = 100)
    @Column(name = "numero_bolla", nullable = false, length = 100)
    private String numeroBolla;

    @NotNull
    @Column(name = "data_bolla", nullable = false)
    private LocalDate dataBolla;

    @Column(name = "data_arrivo", nullable = true)
    private LocalDate dataArrivo;

    @NotBlank
    @Size(max = 8)
    @Pattern(regexp = "ARRIVO|USCITA")
    @Column(name = "direzione", nullable = false, length = 8)
    private String direzione;

    protected Bolla() {
    }

    public Bolla(Fornitore fornitore, String numeroBolla, LocalDate dataBolla, LocalDate dataArrivo, String direzione) {
        this.fornitore = fornitore;
        this.numeroBolla = numeroBolla;
        this.dataBolla = dataBolla;
        this.dataArrivo = dataArrivo;
        this.direzione = direzione;
    }

    public UUID getId() {
        return id;
    }

    public Fornitore getFornitore() {
        return fornitore;
    }

    public void setFornitore(Fornitore fornitore) {
        this.fornitore = fornitore;
    }

    public String getNumeroBolla() {
        return numeroBolla;
    }

    public void setNumeroBolla(String numeroBolla) {
        this.numeroBolla = numeroBolla;
    }

    public LocalDate getDataBolla() {
        return dataBolla;
    }

    public void setDataBolla(LocalDate dataBolla) {
        this.dataBolla = dataBolla;
    }

    public LocalDate getDataArrivo() {
        return dataArrivo;
    }

    public void setDataArrivo(LocalDate dataArrivo) {
        this.dataArrivo = dataArrivo;
    }

    public String getDirezione() {
        return direzione;
    }

    public void setDirezione(String direzione) {
        this.direzione = direzione;
    }
}

