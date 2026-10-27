package it.sectec.platform.inventory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Entity
@Table(name = "fornitore")
public class Fornitore {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "ragione_sociale", nullable = false, length = 255)
    private String ragioneSociale;

    @NotBlank
    @Size(max = 255)
    @Column(name = "indirizzo", nullable = false, length = 255)
    private String indirizzo;

    @Size(max = 20)
    @Column(name = "partita_iva", nullable = true, length = 20)
    private String partitaIva;

    @Size(max = 16)
    @Column(name = "codice_fiscale", nullable = true, length = 16)
    private String codiceFiscale;

    @Size(max = 32)
    @Column(name = "telefono", nullable = true, length = 32)
    private String telefono;

    @Size(max = 254)
    @Column(name = "email", nullable = true, length = 254)
    private String email;

    @Size(max = 34)
    @Column(name = "iban", nullable = true, length = 34)
    private String iban;

    protected Fornitore() {
    }

    public Fornitore(String ragioneSociale, String indirizzo, String partitaIva, String codiceFiscale, String telefono, String email, String iban) {
        this.ragioneSociale = ragioneSociale;
        this.indirizzo = indirizzo;
        this.partitaIva = partitaIva;
        this.codiceFiscale = codiceFiscale;
        this.telefono = telefono;
        this.email = email;
        this.iban = iban;
    }

    public UUID getId() {
        return id;
    }

    public String getRagioneSociale() {
        return ragioneSociale;
    }

    public void setRagioneSociale(String ragioneSociale) {
        this.ragioneSociale = ragioneSociale;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public String getPartitaIva() {
        return partitaIva;
    }

    public void setPartitaIva(String partitaIva) {
        this.partitaIva = partitaIva;
    }

    public String getCodiceFiscale() {
        return codiceFiscale;
    }

    public void setCodiceFiscale(String codiceFiscale) {
        this.codiceFiscale = codiceFiscale;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }
}

