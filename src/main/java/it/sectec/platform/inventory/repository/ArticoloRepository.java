package it.sectec.platform.inventory.repository;

import it.sectec.platform.inventory.entity.Articolo;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticoloRepository extends JpaRepository<Articolo, UUID> {}

