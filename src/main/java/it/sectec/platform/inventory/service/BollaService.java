package it.sectec.platform.inventory.service;

import it.sectec.platform.inventory.entity.Bolla;
import it.sectec.platform.inventory.entity.Fornitore;
import it.sectec.platform.inventory.dto.BollaDto.*;
import it.sectec.platform.inventory.repository.BollaRepository;
import it.sectec.platform.shared.crud.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
@Transactional
public class BollaService extends CrudService<Bolla, Request, Response> {
    private final EntityReferences references;

    public BollaService(BollaRepository repository, EntityReferences references) {
        super(repository);
        this.references = references;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_BOLLA_READ')")
    public PageResponse<Response> list(int page, int size) { return super.list(page, size); }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_BOLLA_READ')")
    public Response get(UUID id) { return super.get(id); }

    @Override
    @PreAuthorize("hasAuthority('PERM_BOLLA_WRITE')")
    public Response create(Request request) { return super.create(request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_BOLLA_WRITE')")
    public Response update(UUID id, Request request) { return super.update(id, request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_BOLLA_WRITE')")
    public void delete(UUID id) { super.delete(id); }

    @Override
    protected Bolla createEntity(Request request) {
        return new Bolla(references.find(Fornitore.class, request.fornitoreId()),
                request.numeroBolla(),
                request.dataBolla(),
                request.dataArrivo(),
                request.direzione());
    }

    @Override
    protected void updateEntity(Bolla entity, Request request) {
        entity.setFornitore(references.find(Fornitore.class, request.fornitoreId()));
        entity.setNumeroBolla(request.numeroBolla());
        entity.setDataBolla(request.dataBolla());
        entity.setDataArrivo(request.dataArrivo());
        entity.setDirezione(request.direzione());
    }

    @Override
    protected Response response(Bolla entity) {
        return new Response(entity.getId(), entity.getFornitore() == null ? null : entity.getFornitore().getId(),
                entity.getNumeroBolla(),
                entity.getDataBolla(),
                entity.getDataArrivo(),
                entity.getDirezione());
    }
}

