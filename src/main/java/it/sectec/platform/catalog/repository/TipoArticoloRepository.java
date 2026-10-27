package it.sectec.platform.catalog.repository;

import it.sectec.platform.catalog.entity.TipoArticolo;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoArticoloRepository extends JpaRepository<TipoArticolo, UUID> {}

