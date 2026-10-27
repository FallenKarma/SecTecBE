package it.sectec.platform.support.service;

import it.sectec.platform.support.entity.TipoAssistenza;

import it.sectec.platform.support.dto.TipoAssistenzaDto.*;
import it.sectec.platform.support.repository.TipoAssistenzaRepository;
import it.sectec.platform.shared.crud.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
@Transactional
public class TipoAssistenzaService extends CrudService<TipoAssistenza, Request, Response> {
    private final EntityReferences references;

    public TipoAssistenzaService(TipoAssistenzaRepository repository, EntityReferences references) {
        super(repository);
        this.references = references;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_TIPO_ASSISTENZA_READ')")
    public PageResponse<Response> list(int page, int size) { return super.list(page, size); }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_TIPO_ASSISTENZA_READ')")
    public Response get(UUID id) { return super.get(id); }

    @Override
    @PreAuthorize("hasAuthority('PERM_TIPO_ASSISTENZA_WRITE')")
    public Response create(Request request) { return super.create(request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_TIPO_ASSISTENZA_WRITE')")
    public Response update(UUID id, Request request) { return super.update(id, request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_TIPO_ASSISTENZA_WRITE')")
    public void delete(UUID id) { super.delete(id); }

    @Override
    protected TipoAssistenza createEntity(Request request) {
        return new TipoAssistenza(request.nome());
    }

    @Override
    protected void updateEntity(TipoAssistenza entity, Request request) {
        entity.setNome(request.nome());
    }

    @Override
    protected Response response(TipoAssistenza entity) {
        return new Response(entity.getId(), entity.getNome());
    }
}

