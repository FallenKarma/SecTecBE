package it.sectec.platform.identity.repository;

import it.sectec.platform.identity.entity.Role;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(String name);
}

