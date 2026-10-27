package it.sectec.platform.catalog.service;

import it.sectec.platform.catalog.entity.ImmagineArticolo;
import it.sectec.platform.storage.entity.FileAsset;
import it.sectec.platform.catalog.entity.TipoArticolo;
import it.sectec.platform.catalog.dto.ImmagineArticoloDto.*;
import it.sectec.platform.catalog.repository.ImmagineArticoloRepository;
import it.sectec.platform.shared.crud.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
@Transactional
public class ImmagineArticoloService extends CrudService<ImmagineArticolo, Request, Response> {
    private final EntityReferences references;

    public ImmagineArticoloService(ImmagineArticoloRepository repository, EntityReferences references) {
        super(repository);
        this.references = references;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_IMMAGINE_ARTICOLO_READ')")
    public PageResponse<Response> list(int page, int size) { return super.list(page, size); }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_IMMAGINE_ARTICOLO_READ')")
    public Response get(UUID id) { return super.get(id); }

    @Override
    @PreAuthorize("hasAuthority('PERM_IMMAGINE_ARTICOLO_WRITE')")
    public Response create(Request request) { return super.create(request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_IMMAGINE_ARTICOLO_WRITE')")
    public Response update(UUID id, Request request) { return super.update(id, request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_IMMAGINE_ARTICOLO_WRITE')")
    public void delete(UUID id) { super.delete(id); }

    @Override
    protected ImmagineArticolo createEntity(Request request) {
        return new ImmagineArticolo(references.find(FileAsset.class, request.fileAssetId()),
                request.altText(),
                request.sortOrder(),
                references.find(TipoArticolo.class, request.tipoArticoloId()));
    }

    @Override
    protected void updateEntity(ImmagineArticolo entity, Request request) {
        entity.setFileAsset(references.find(FileAsset.class, request.fileAssetId()));
        entity.setAltText(request.altText());
        entity.setSortOrder(request.sortOrder());
        entity.setTipoArticolo(references.find(TipoArticolo.class, request.tipoArticoloId()));
    }

    @Override
    protected Response response(ImmagineArticolo entity) {
        return new Response(entity.getId(), entity.getFileAsset() == null ? null : entity.getFileAsset().getId(),
                entity.getAltText(),
                entity.getSortOrder(),
                entity.getTipoArticolo() == null ? null : entity.getTipoArticolo().getId());
    }
}

