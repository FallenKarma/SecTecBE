package it.sectec.platform.catalog.service;

import it.sectec.platform.catalog.entity.DocumentoArticolo;
import it.sectec.platform.storage.entity.FileAsset;
import it.sectec.platform.catalog.entity.TipoArticolo;
import it.sectec.platform.catalog.dto.DocumentoArticoloDto.*;
import it.sectec.platform.catalog.repository.DocumentoArticoloRepository;
import it.sectec.platform.shared.crud.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
@Transactional
public class DocumentoArticoloService extends CrudService<DocumentoArticolo, Request, Response> {
    private final EntityReferences references;

    public DocumentoArticoloService(DocumentoArticoloRepository repository, EntityReferences references) {
        super(repository);
        this.references = references;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_DOCUMENTO_ARTICOLO_READ')")
    public PageResponse<Response> list(int page, int size) { return super.list(page, size); }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_DOCUMENTO_ARTICOLO_READ')")
    public Response get(UUID id) { return super.get(id); }

    @Override
    @PreAuthorize("hasAuthority('PERM_DOCUMENTO_ARTICOLO_WRITE')")
    public Response create(Request request) { return super.create(request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_DOCUMENTO_ARTICOLO_WRITE')")
    public Response update(UUID id, Request request) { return super.update(id, request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_DOCUMENTO_ARTICOLO_WRITE')")
    public void delete(UUID id) { super.delete(id); }

    @Override
    protected DocumentoArticolo createEntity(Request request) {
        return new DocumentoArticolo(references.find(FileAsset.class, request.fileAssetId()),
                request.version(),
                references.find(TipoArticolo.class, request.tipoArticoloId()));
    }

    @Override
    protected void updateEntity(DocumentoArticolo entity, Request request) {
        entity.setFileAsset(references.find(FileAsset.class, request.fileAssetId()));
        entity.setVersion(request.version());
        entity.setTipoArticolo(references.find(TipoArticolo.class, request.tipoArticoloId()));
    }

    @Override
    protected Response response(DocumentoArticolo entity) {
        return new Response(entity.getId(), entity.getFileAsset() == null ? null : entity.getFileAsset().getId(),
                entity.getVersion(),
                entity.getTipoArticolo() == null ? null : entity.getTipoArticolo().getId());
    }
}

