package it.sectec.platform.installation.repository;

import it.sectec.platform.installation.entity.ClienteImpianto;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteImpiantoRepository extends JpaRepository<ClienteImpianto, UUID> {
    @org.springframework.data.jpa.repository.Query("select ci from ClienteImpianto ci join fetch ci.cliente join fetch ci.impianto")
    java.util.List<ClienteImpianto> findAllWithClient();
    @org.springframework.data.jpa.repository.Query("select ci from ClienteImpianto ci join fetch ci.cliente where ci.impianto.id = :id order by ci.cliente.cognome, ci.cliente.nome, ci.cliente.id")
    java.util.List<ClienteImpianto> findForInstallation(UUID id);
    void deleteAllByImpiantoId(UUID id);
    void deleteAllByClienteId(UUID id);
}

