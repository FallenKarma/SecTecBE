package it.sectec.platform.support.repository;

import it.sectec.platform.support.entity.UserIntervento;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInterventoRepository extends JpaRepository<UserIntervento, UUID> {}

