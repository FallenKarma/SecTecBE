package it.sectec.platform.identity.repository;

import it.sectec.platform.identity.entity.UserRole;
import org.springframework.data.jpa.repository.*;
import java.util.List;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
    @EntityGraph(attributePaths = "role")
    List<UserRole> findByUser_Id(UUID userId);
}

