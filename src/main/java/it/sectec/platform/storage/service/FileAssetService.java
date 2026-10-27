package it.sectec.platform.storage.service;

import it.sectec.platform.storage.entity.FileAsset;

import it.sectec.platform.storage.dto.FileAssetDto.*;
import it.sectec.platform.storage.repository.FileAssetRepository;
import it.sectec.platform.shared.crud.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
@Transactional
public class FileAssetService extends CrudService<FileAsset, Request, Response> {
    private final EntityReferences references;

    public FileAssetService(FileAssetRepository repository, EntityReferences references) {
        super(repository);
        this.references = references;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_FILE_ASSET_READ')")
    public PageResponse<Response> list(int page, int size) { return super.list(page, size); }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_FILE_ASSET_READ')")
    public Response get(UUID id) { return super.get(id); }

    @Override
    @PreAuthorize("hasAuthority('PERM_FILE_ASSET_WRITE')")
    public Response create(Request request) { return super.create(request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_FILE_ASSET_WRITE')")
    public Response update(UUID id, Request request) { return super.update(id, request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_FILE_ASSET_WRITE')")
    public void delete(UUID id) { super.delete(id); }

    @Override
    protected FileAsset createEntity(Request request) {
        return new FileAsset(request.storageBackend(),
                request.storageNamespace(),
                request.storageKey(),
                request.originalName(),
                request.mimeType(),
                request.byteSize(),
                request.sha256(),
                request.visibility(),
                request.createdAt());
    }

    @Override
    protected void updateEntity(FileAsset entity, Request request) {
        entity.setStorageBackend(request.storageBackend());
        entity.setStorageNamespace(request.storageNamespace());
        entity.setStorageKey(request.storageKey());
        entity.setOriginalName(request.originalName());
        entity.setMimeType(request.mimeType());
        entity.setByteSize(request.byteSize());
        entity.setSha256(request.sha256());
        entity.setVisibility(request.visibility());
        entity.setCreatedAt(request.createdAt());
    }

    @Override
    protected Response response(FileAsset entity) {
        return new Response(entity.getId(), entity.getStorageBackend(),
                entity.getStorageNamespace(),
                entity.getStorageKey(),
                entity.getOriginalName(),
                entity.getMimeType(),
                entity.getByteSize(),
                entity.getSha256(),
                entity.getVisibility(),
                entity.getCreatedAt());
    }
}

