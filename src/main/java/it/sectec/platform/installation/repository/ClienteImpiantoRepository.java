package it.sectec.platform.installation.repository;

import it.sectec.platform.installation.entity.ClienteImpianto;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteImpiantoRepository extends JpaRepository<ClienteImpianto, UUID> {}

