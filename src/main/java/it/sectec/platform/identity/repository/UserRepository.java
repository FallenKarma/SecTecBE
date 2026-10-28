package it.sectec.platform.identity.repository;

import it.sectec.platform.identity.entity.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where lower(trim(u.email)) = lower(trim(:email))")
    Optional<User> findLockedByEmail(@Param("email") String email);
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findLockedById(@Param("id") UUID id);
    @Query("select u from User u where lower(trim(u.email)) = lower(trim(:email))")
    Optional<User> findByEmail(@Param("email") String email);
}

