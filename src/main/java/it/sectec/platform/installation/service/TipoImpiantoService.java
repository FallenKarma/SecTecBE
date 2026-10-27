package it.sectec.platform.installation.service;

import it.sectec.platform.installation.entity.TipoImpianto;

import it.sectec.platform.installation.dto.TipoImpiantoDto.*;
import it.sectec.platform.installation.repository.TipoImpiantoRepository;
import it.sectec.platform.shared.crud.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
@Transactional
public class TipoImpiantoService extends CrudService<TipoImpianto, Request, Response> {
    private final EntityReferences references;

    public TipoImpiantoService(TipoImpiantoRepository repository, EntityReferences references) {
        super(repository);
        this.references = references;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_TIPO_IMPIANTO_READ')")
    public PageResponse<Response> list(int page, int size) { return super.list(page, size); }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_TIPO_IMPIANTO_READ')")
    public Response get(UUID id) { return super.get(id); }

    @Override
    @PreAuthorize("hasAuthority('PERM_TIPO_IMPIANTO_WRITE')")
    public Response create(Request request) { return super.create(request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_TIPO_IMPIANTO_WRITE')")
    public Response update(UUID id, Request request) { return super.update(id, request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_TIPO_IMPIANTO_WRITE')")
    public void delete(UUID id) { super.delete(id); }

    @Override
    protected TipoImpianto createEntity(Request request) {
        return new TipoImpianto(request.nome());
    }

    @Override
    protected void updateEntity(TipoImpianto entity, Request request) {
        entity.setNome(request.nome());
    }

    @Override
    protected Response response(TipoImpianto entity) {
        return new Response(entity.getId(), entity.getNome());
    }
}

