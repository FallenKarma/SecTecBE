package it.sectec.platform.catalog.repository;

import it.sectec.platform.catalog.entity.Categoria;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {}

