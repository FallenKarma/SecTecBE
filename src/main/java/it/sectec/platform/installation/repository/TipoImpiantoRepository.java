package it.sectec.platform.installation.repository;

import it.sectec.platform.installation.entity.TipoImpianto;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoImpiantoRepository extends JpaRepository<TipoImpianto, UUID> {}

