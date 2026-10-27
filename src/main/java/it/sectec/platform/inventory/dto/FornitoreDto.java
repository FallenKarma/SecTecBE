package it.sectec.platform.inventory.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;

public final class FornitoreDto {
    private FornitoreDto() {}
    public record Request(
            @NotBlank @Size(max = 255) String ragioneSociale,
            @NotBlank @Size(max = 255) String indirizzo,
            @Size(max = 20) String partitaIva,
            @Size(max = 16) String codiceFiscale,
            @Size(max = 32) String telefono,
            @Size(max = 254) String email,
            @Size(max = 34) String iban) {}
    public record Response(UUID id,
            String ragioneSociale,
            String indirizzo,
            String partitaIva,
            String codiceFiscale,
            String telefono,
            String email,
            String iban) {}
}

