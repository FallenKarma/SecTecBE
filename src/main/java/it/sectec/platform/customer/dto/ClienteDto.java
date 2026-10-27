package it.sectec.platform.customer.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;

public final class ClienteDto {
    private ClienteDto() {}
    public record Request(
            UUID userId,
            @Size(max = 255) String indirizzo,
            @Size(max = 16) String cap,
            @Size(max = 16) String codiceFiscale,
            @Size(max = 20) String partitaIva,
            @Size(max = 254) String pec,
            LocalDate dataNascita,
            @NotBlank @Size(max = 100) String nome,
            @NotBlank @Size(max = 100) String cognome,
            @Size(max = 100) String iban,
            @Size(max = 255) String indirizzoFatturazione) {}
    public record Response(UUID id,
            UUID userId,
            String indirizzo,
            String cap,
            String codiceFiscale,
            String partitaIva,
            String pec,
            LocalDate dataNascita,
            String nome,
            String cognome,
            String iban,
            String indirizzoFatturazione) {}
}

