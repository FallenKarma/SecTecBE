package it.sectec.platform.catalog.repository;

import it.sectec.platform.catalog.entity.DocumentoArticolo;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoArticoloRepository extends JpaRepository<DocumentoArticolo, UUID> {}

