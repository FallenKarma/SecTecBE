package it.sectec.platform.support.repository;

import it.sectec.platform.support.entity.Assistenza;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssistenzaRepository extends JpaRepository<Assistenza, UUID> {}

