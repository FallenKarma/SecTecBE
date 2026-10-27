package it.sectec.platform.catalog.repository;

import it.sectec.platform.catalog.entity.ImmagineArticolo;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImmagineArticoloRepository extends JpaRepository<ImmagineArticolo, UUID> {}

