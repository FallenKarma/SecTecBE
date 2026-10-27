package it.sectec.platform.inventory.repository;

import it.sectec.platform.inventory.entity.BollaTipoArticolo;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BollaTipoArticoloRepository extends JpaRepository<BollaTipoArticolo, UUID> {}

