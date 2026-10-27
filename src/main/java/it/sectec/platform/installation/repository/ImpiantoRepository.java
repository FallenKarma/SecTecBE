package it.sectec.platform.installation.repository;

import it.sectec.platform.installation.entity.Impianto;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImpiantoRepository extends JpaRepository<Impianto, UUID> {}

