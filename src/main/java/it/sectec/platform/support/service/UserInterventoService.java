package it.sectec.platform.support.service;

import it.sectec.platform.support.entity.UserIntervento;
import it.sectec.platform.support.entity.Intervento;
import it.sectec.platform.identity.entity.User;
import it.sectec.platform.support.dto.UserInterventoDto.*;
import it.sectec.platform.support.repository.UserInterventoRepository;
import it.sectec.platform.shared.crud.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
@Transactional
public class UserInterventoService extends CrudService<UserIntervento, Request, Response> {
    private final EntityReferences references;

    public UserInterventoService(UserInterventoRepository repository, EntityReferences references) {
        super(repository);
        this.references = references;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_USER_INTERVENTO_READ')")
    public PageResponse<Response> list(int page, int size) { return super.list(page, size); }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_USER_INTERVENTO_READ')")
    public Response get(UUID id) { return super.get(id); }

    @Override
    @PreAuthorize("hasAuthority('PERM_USER_INTERVENTO_WRITE')")
    public Response create(Request request) { return super.create(request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_USER_INTERVENTO_WRITE')")
    public Response update(UUID id, Request request) { return super.update(id, request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_USER_INTERVENTO_WRITE')")
    public void delete(UUID id) { super.delete(id); }

    @Override
    protected UserIntervento createEntity(Request request) {
        return new UserIntervento(references.find(Intervento.class, request.interventoId()),
                references.find(User.class, request.userId()));
    }

    @Override
    protected void updateEntity(UserIntervento entity, Request request) {
        entity.setIntervento(references.find(Intervento.class, request.interventoId()));
        entity.setUser(references.find(User.class, request.userId()));
    }

    @Override
    protected Response response(UserIntervento entity) {
        return new Response(entity.getId(), entity.getIntervento() == null ? null : entity.getIntervento().getId(),
                entity.getUser() == null ? null : entity.getUser().getId());
    }
}

