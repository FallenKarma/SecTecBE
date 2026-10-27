package it.sectec.platform.identity.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "app_user")
public class User {

    @Column(name = "credentials_version", nullable = false)
    private long credentialsVersion;

    public long getCredentialsVersion() { return credentialsVersion; }
    public void invalidateCredentials() { credentialsVersion++; }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(max = 254)
    @Column(name = "email", nullable = false, length = 254)
    private String email;

    @NotBlank
    @Size(max = 255)
    @Column(name = "hashed_password", nullable = false, length = 255)
    private String hashedPassword;

    @NotBlank
    @Size(max = 32)
    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    @NotBlank
    @Size(max = 100)
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @NotBlank
    @Size(max = 100)
    @Column(name = "cognome", nullable = false, length = 100)
    private String cognome;

    protected User() {
    }

    public User(String email, String hashedPassword, String status, Instant emailVerifiedAt, String nome, String cognome) {
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.status = status;
        this.emailVerifiedAt = emailVerifiedAt;
        this.nome = nome;
        this.cognome = cognome;
    }

    public UUID getId() {
        return id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setEmailVerifiedAt(Instant emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getEmail() {
        return email;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public String getStatus() {
        return status;
    }

    public Instant getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }
}

