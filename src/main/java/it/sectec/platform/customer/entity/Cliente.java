package it.sectec.platform.customer.entity;

import it.sectec.platform.identity.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", nullable = true, unique = true)
    private User user;

    @Size(max = 255)
    @Column(name = "indirizzo", nullable = true, length = 255)
    private String indirizzo;

    @Size(max = 16)
    @Column(name = "cap", nullable = true, length = 16)
    private String cap;

    @Size(max = 16)
    @Column(name = "codice_fiscale", nullable = true, length = 16)
    private String codiceFiscale;

    @Size(max = 20)
    @Column(name = "partita_iva", nullable = true, length = 20)
    private String partitaIva;

    @Size(max = 254)
    @Column(name = "pec", nullable = true, length = 254)
    private String pec;

    @Column(name = "data_nascita", nullable = true)
    private LocalDate dataNascita;

    @NotBlank
    @Size(max = 100)
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @NotBlank
    @Size(max = 100)
    @Column(name = "cognome", nullable = false, length = 100)
    private String cognome;

    @Size(max = 100)
    @Column(name = "iban", nullable = true, length = 100)
    private String iban;

    @Size(max = 255)
    @Column(name = "indirizzo_fatturazione", nullable = true, length = 255)
    private String indirizzoFatturazione;

    protected Cliente() {
    }

    public Cliente(User user, String indirizzo, String cap, String codiceFiscale, String partitaIva, String pec, LocalDate dataNascita, String nome, String cognome, String iban, String indirizzoFatturazione) {
        this.user = user;
        this.indirizzo = indirizzo;
        this.cap = cap;
        this.codiceFiscale = codiceFiscale;
        this.partitaIva = partitaIva;
        this.pec = pec;
        this.dataNascita = dataNascita;
        this.nome = nome;
        this.cognome = cognome;
        this.iban = iban;
        this.indirizzoFatturazione = indirizzoFatturazione;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public String getCodiceFiscale() {
        return codiceFiscale;
    }

    public void setCodiceFiscale(String codiceFiscale) {
        this.codiceFiscale = codiceFiscale;
    }

    public String getPartitaIva() {
        return partitaIva;
    }

    public void setPartitaIva(String partitaIva) {
        this.partitaIva = partitaIva;
    }

    public String getPec() {
        return pec;
    }

    public void setPec(String pec) {
        this.pec = pec;
    }

    public LocalDate getDataNascita() {
        return dataNascita;
    }

    public void setDataNascita(LocalDate dataNascita) {
        this.dataNascita = dataNascita;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getIndirizzoFatturazione() {
        return indirizzoFatturazione;
    }

    public void setIndirizzoFatturazione(String indirizzoFatturazione) {
        this.indirizzoFatturazione = indirizzoFatturazione;
    }
}

