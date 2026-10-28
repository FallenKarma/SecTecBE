package it.sectec.platform.installation.repository;

import it.sectec.platform.installation.entity.Impianto;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImpiantoRepository extends JpaRepository<Impianto, UUID> {
    @org.springframework.data.jpa.repository.Query("""
            select ci.impianto from ClienteImpianto ci
            where ci.cliente.id = :clienteId order by ci.impianto.id
            """)
    java.util.List<Impianto> findAllByClienteId(@org.springframework.data.repository.query.Param("clienteId") UUID clienteId);
}

