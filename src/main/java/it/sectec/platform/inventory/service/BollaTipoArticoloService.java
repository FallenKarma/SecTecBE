package it.sectec.platform.inventory.service;

import it.sectec.platform.inventory.entity.BollaTipoArticolo;
import it.sectec.platform.inventory.entity.Bolla;
import it.sectec.platform.catalog.entity.TipoArticolo;
import it.sectec.platform.inventory.dto.BollaTipoArticoloDto.*;
import it.sectec.platform.inventory.repository.BollaTipoArticoloRepository;
import it.sectec.platform.shared.crud.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
@Transactional
public class BollaTipoArticoloService extends CrudService<BollaTipoArticolo, Request, Response> {
    private final EntityReferences references;

    public BollaTipoArticoloService(BollaTipoArticoloRepository repository, EntityReferences references) {
        super(repository);
        this.references = references;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_BOLLA_TIPO_ARTICOLO_READ')")
    public PageResponse<Response> list(int page, int size) { return super.list(page, size); }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_BOLLA_TIPO_ARTICOLO_READ')")
    public Response get(UUID id) { return super.get(id); }

    @Override
    @PreAuthorize("hasAuthority('PERM_BOLLA_TIPO_ARTICOLO_WRITE')")
    public Response create(Request request) { return super.create(request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_BOLLA_TIPO_ARTICOLO_WRITE')")
    public Response update(UUID id, Request request) { return super.update(id, request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_BOLLA_TIPO_ARTICOLO_WRITE')")
    public void delete(UUID id) { super.delete(id); }

    @Override
    protected BollaTipoArticolo createEntity(Request request) {
        return new BollaTipoArticolo(references.find(Bolla.class, request.bollaId()),
                references.find(TipoArticolo.class, request.tipoArticoloId()),
                request.quantita());
    }

    @Override
    protected void updateEntity(BollaTipoArticolo entity, Request request) {
        entity.setBolla(references.find(Bolla.class, request.bollaId()));
        entity.setTipoArticolo(references.find(TipoArticolo.class, request.tipoArticoloId()));
        entity.setQuantita(request.quantita());
    }

    @Override
    protected Response response(BollaTipoArticolo entity) {
        return new Response(entity.getId(), entity.getBolla() == null ? null : entity.getBolla().getId(),
                entity.getTipoArticolo() == null ? null : entity.getTipoArticolo().getId(),
                entity.getQuantita());
    }
}

