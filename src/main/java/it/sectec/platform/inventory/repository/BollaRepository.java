package it.sectec.platform.inventory.repository;

import it.sectec.platform.inventory.entity.Bolla;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BollaRepository extends JpaRepository<Bolla, UUID> {}

