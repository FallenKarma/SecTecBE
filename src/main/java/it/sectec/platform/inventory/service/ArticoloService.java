package it.sectec.platform.inventory.service;

import it.sectec.platform.inventory.entity.Articolo;
import it.sectec.platform.catalog.entity.TipoArticolo;
import it.sectec.platform.installation.entity.Impianto;
import it.sectec.platform.inventory.dto.ArticoloDto.*;
import it.sectec.platform.inventory.repository.ArticoloRepository;
import it.sectec.platform.shared.crud.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
@Transactional
public class ArticoloService extends CrudService<Articolo, Request, Response> {
    private final EntityReferences references;

    public ArticoloService(ArticoloRepository repository, EntityReferences references) {
        super(repository);
        this.references = references;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_ARTICOLO_READ')")
    public PageResponse<Response> list(int page, int size) { return super.list(page, size); }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_ARTICOLO_READ')")
    public Response get(UUID id) { return super.get(id); }

    @Override
    @PreAuthorize("hasAuthority('PERM_ARTICOLO_WRITE')")
    public Response create(Request request) { return super.create(request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_ARTICOLO_WRITE')")
    public Response update(UUID id, Request request) { return super.update(id, request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_ARTICOLO_WRITE')")
    public void delete(UUID id) { super.delete(id); }

    @Override
    protected Articolo createEntity(Request request) {
        return new Articolo(references.find(TipoArticolo.class, request.tipoArticoloId()),
                references.find(Impianto.class, request.impiantoId()),
                request.matricola(),
                request.stato());
    }

    @Override
    protected void updateEntity(Articolo entity, Request request) {
        entity.setTipoArticolo(references.find(TipoArticolo.class, request.tipoArticoloId()));
        entity.setImpianto(references.find(Impianto.class, request.impiantoId()));
        entity.setMatricola(request.matricola());
        entity.setStato(request.stato());
    }

    @Override
    protected Response response(Articolo entity) {
        return new Response(entity.getId(), entity.getTipoArticolo() == null ? null : entity.getTipoArticolo().getId(),
                entity.getImpianto() == null ? null : entity.getImpianto().getId(),
                entity.getMatricola(),
                entity.getStato());
    }
}

