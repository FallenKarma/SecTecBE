package it.sectec.platform.identity.service;

import it.sectec.platform.identity.entity.Permission;

import it.sectec.platform.identity.dto.PermissionDto.*;
import it.sectec.platform.identity.repository.PermissionRepository;
import it.sectec.platform.shared.crud.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
@Transactional
public class PermissionService extends CrudService<Permission, Request, Response> {
    private final EntityReferences references;

    public PermissionService(PermissionRepository repository, EntityReferences references) {
        super(repository);
        this.references = references;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_PERMISSION_READ')")
    public PageResponse<Response> list(int page, int size) { return super.list(page, size); }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_PERMISSION_READ')")
    public Response get(UUID id) { return super.get(id); }

    @Override
    @PreAuthorize("hasAuthority('PERM_PERMISSION_WRITE')")
    public Response create(Request request) { return super.create(request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_PERMISSION_WRITE')")
    public Response update(UUID id, Request request) { return super.update(id, request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_PERMISSION_WRITE')")
    public void delete(UUID id) { super.delete(id); }

    @Override
    protected Permission createEntity(Request request) {
        return new Permission(request.name(),
                request.description());
    }

    @Override
    protected void updateEntity(Permission entity, Request request) {
        entity.setName(request.name());
        entity.setDescription(request.description());
    }

    @Override
    protected Response response(Permission entity) {
        return new Response(entity.getId(), entity.getName(),
                entity.getDescription());
    }
}

