package it.sectec.platform.identity.repository;

import it.sectec.platform.identity.entity.RolePermission;
import org.springframework.data.jpa.repository.*;
import java.util.List;
import java.util.UUID;

public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {
    @EntityGraph(attributePaths = "permission")
    List<RolePermission> findByRole_Id(UUID roleId);
}

