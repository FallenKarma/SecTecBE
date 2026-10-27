package it.sectec.platform.storage.repository;

import it.sectec.platform.storage.entity.FileAsset;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileAssetRepository extends JpaRepository<FileAsset, UUID> {}

