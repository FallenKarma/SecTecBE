package it.sectec.platform.inventory.repository;

import it.sectec.platform.inventory.entity.Fornitore;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FornitoreRepository extends JpaRepository<Fornitore, UUID> {}

