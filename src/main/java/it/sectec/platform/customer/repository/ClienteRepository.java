package it.sectec.platform.customer.repository;

import it.sectec.platform.customer.entity.Cliente;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {}

