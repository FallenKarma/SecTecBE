package it.sectec.platform.support.repository;

import it.sectec.platform.support.entity.TipoIntervento;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoInterventoRepository extends JpaRepository<TipoIntervento, UUID> {}

