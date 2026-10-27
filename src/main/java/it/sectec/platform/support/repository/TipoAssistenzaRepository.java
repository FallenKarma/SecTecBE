package it.sectec.platform.support.repository;

import it.sectec.platform.support.entity.TipoAssistenza;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoAssistenzaRepository extends JpaRepository<TipoAssistenza, UUID> {}

