package it.sectec.platform.support.service;

import it.sectec.platform.support.entity.TipoIntervento;

import it.sectec.platform.support.dto.TipoInterventoDto.*;
import it.sectec.platform.support.repository.TipoInterventoRepository;
import it.sectec.platform.shared.crud.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
@Transactional
public class TipoInterventoService extends CrudService<TipoIntervento, Request, Response> {
    private final EntityReferences references;

    public TipoInterventoService(TipoInterventoRepository repository, EntityReferences references) {
        super(repository);
        this.references = references;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_TIPO_INTERVENTO_READ')")
    public PageResponse<Response> list(int page, int size) { return super.list(page, size); }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_TIPO_INTERVENTO_READ')")
    public Response get(UUID id) { return super.get(id); }

    @Override
    @PreAuthorize("hasAuthority('PERM_TIPO_INTERVENTO_WRITE')")
    public Response create(Request request) { return super.create(request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_TIPO_INTERVENTO_WRITE')")
    public Response update(UUID id, Request request) { return super.update(id, request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_TIPO_INTERVENTO_WRITE')")
    public void delete(UUID id) { super.delete(id); }

    @Override
    protected TipoIntervento createEntity(Request request) {
        return new TipoIntervento(request.nome());
    }

    @Override
    protected void updateEntity(TipoIntervento entity, Request request) {
        entity.setNome(request.nome());
    }

    @Override
    protected Response response(TipoIntervento entity) {
        return new Response(entity.getId(), entity.getNome());
    }
}

