package it.sectec.platform.inventory.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;

public final class BollaDto {
    private BollaDto() {}
    public record Request(
            UUID fornitoreId,
            @NotBlank @Size(max = 100) String numeroBolla,
            @NotNull LocalDate dataBolla,
            LocalDate dataArrivo,
            @NotBlank @Size(max = 8) @Pattern(regexp = "ARRIVO|USCITA") String direzione) {}
    public record Response(UUID id,
            UUID fornitoreId,
            String numeroBolla,
            LocalDate dataBolla,
            LocalDate dataArrivo,
            String direzione) {}
}

