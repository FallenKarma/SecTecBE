package it.sectec.platform.identity.service;

import it.sectec.platform.identity.entity.Role;

import it.sectec.platform.identity.dto.RoleDto.*;
import it.sectec.platform.identity.repository.RoleRepository;
import it.sectec.platform.shared.crud.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
@Transactional
public class RoleService extends CrudService<Role, Request, Response> {
    private final EntityReferences references;

    public RoleService(RoleRepository repository, EntityReferences references) {
        super(repository);
        this.references = references;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_ROLE_READ')")
    public PageResponse<Response> list(int page, int size) { return super.list(page, size); }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_ROLE_READ')")
    public Response get(UUID id) { return super.get(id); }

    @Override
    @PreAuthorize("hasAuthority('PERM_ROLE_WRITE')")
    public Response create(Request request) { return super.create(request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_ROLE_WRITE')")
    public Response update(UUID id, Request request) { return super.update(id, request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_ROLE_WRITE')")
    public void delete(UUID id) { super.delete(id); }

    @Override
    protected Role createEntity(Request request) {
        return new Role(request.name(),
                request.description());
    }

    @Override
    protected void updateEntity(Role entity, Request request) {
        entity.setName(request.name());
        entity.setDescription(request.description());
    }

    @Override
    protected Response response(Role entity) {
        return new Response(entity.getId(), entity.getName(),
                entity.getDescription());
    }
}

