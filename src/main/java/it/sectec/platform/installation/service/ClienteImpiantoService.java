package it.sectec.platform.installation.service;

import it.sectec.platform.installation.entity.ClienteImpianto;
import it.sectec.platform.customer.entity.Cliente;
import it.sectec.platform.installation.entity.Impianto;
import it.sectec.platform.installation.dto.ClienteImpiantoDto.*;
import it.sectec.platform.installation.repository.ClienteImpiantoRepository;
import it.sectec.platform.shared.crud.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
@Transactional
public class ClienteImpiantoService extends CrudService<ClienteImpianto, Request, Response> {
    private final EntityReferences references;

    public ClienteImpiantoService(ClienteImpiantoRepository repository, EntityReferences references) {
        super(repository);
        this.references = references;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_CLIENTE_IMPIANTO_READ')")
    public PageResponse<Response> list(int page, int size) { return super.list(page, size); }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_CLIENTE_IMPIANTO_READ')")
    public Response get(UUID id) { return super.get(id); }

    @Override
    @PreAuthorize("hasAuthority('PERM_CLIENTE_IMPIANTO_WRITE')")
    public Response create(Request request) { return super.create(request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_CLIENTE_IMPIANTO_WRITE')")
    public Response update(UUID id, Request request) { return super.update(id, request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_CLIENTE_IMPIANTO_WRITE')")
    public void delete(UUID id) { super.delete(id); }

    @Override
    protected ClienteImpianto createEntity(Request request) {
        return new ClienteImpianto(references.find(Cliente.class, request.clienteId()),
                references.find(Impianto.class, request.impiantoId()));
    }

    @Override
    protected void updateEntity(ClienteImpianto entity, Request request) {
        entity.setCliente(references.find(Cliente.class, request.clienteId()));
        entity.setImpianto(references.find(Impianto.class, request.impiantoId()));
    }

    @Override
    protected Response response(ClienteImpianto entity) {
        return new Response(entity.getId(), entity.getCliente() == null ? null : entity.getCliente().getId(),
                entity.getImpianto() == null ? null : entity.getImpianto().getId());
    }
}

