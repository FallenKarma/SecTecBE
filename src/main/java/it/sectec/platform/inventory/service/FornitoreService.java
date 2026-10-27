package it.sectec.platform.inventory.service;

import it.sectec.platform.inventory.entity.Fornitore;

import it.sectec.platform.inventory.dto.FornitoreDto.*;
import it.sectec.platform.inventory.repository.FornitoreRepository;
import it.sectec.platform.shared.crud.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
@Transactional
public class FornitoreService extends CrudService<Fornitore, Request, Response> {
    private final EntityReferences references;

    public FornitoreService(FornitoreRepository repository, EntityReferences references) {
        super(repository);
        this.references = references;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_FORNITORE_READ')")
    public PageResponse<Response> list(int page, int size) { return super.list(page, size); }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_FORNITORE_READ')")
    public Response get(UUID id) { return super.get(id); }

    @Override
    @PreAuthorize("hasAuthority('PERM_FORNITORE_WRITE')")
    public Response create(Request request) { return super.create(request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_FORNITORE_WRITE')")
    public Response update(UUID id, Request request) { return super.update(id, request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_FORNITORE_WRITE')")
    public void delete(UUID id) { super.delete(id); }

    @Override
    protected Fornitore createEntity(Request request) {
        return new Fornitore(request.ragioneSociale(),
                request.indirizzo(),
                request.partitaIva(),
                request.codiceFiscale(),
                request.telefono(),
                request.email(),
                request.iban());
    }

    @Override
    protected void updateEntity(Fornitore entity, Request request) {
        entity.setRagioneSociale(request.ragioneSociale());
        entity.setIndirizzo(request.indirizzo());
        entity.setPartitaIva(request.partitaIva());
        entity.setCodiceFiscale(request.codiceFiscale());
        entity.setTelefono(request.telefono());
        entity.setEmail(request.email());
        entity.setIban(request.iban());
    }

    @Override
    protected Response response(Fornitore entity) {
        return new Response(entity.getId(), entity.getRagioneSociale(),
                entity.getIndirizzo(),
                entity.getPartitaIva(),
                entity.getCodiceFiscale(),
                entity.getTelefono(),
                entity.getEmail(),
                entity.getIban());
    }
}

