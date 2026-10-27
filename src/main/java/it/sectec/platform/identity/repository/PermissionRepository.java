package it.sectec.platform.identity.repository;

import it.sectec.platform.identity.entity.Permission;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByName(String name);
}

