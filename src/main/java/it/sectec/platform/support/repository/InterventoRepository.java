package it.sectec.platform.support.repository;

import it.sectec.platform.support.entity.Intervento;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterventoRepository extends JpaRepository<Intervento, UUID> {}

