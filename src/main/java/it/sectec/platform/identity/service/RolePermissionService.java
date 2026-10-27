package it.sectec.platform.identity.service;

import it.sectec.platform.identity.entity.RolePermission;
import it.sectec.platform.identity.entity.Role;
import it.sectec.platform.identity.entity.Permission;
import it.sectec.platform.identity.dto.RolePermissionDto.*;
import it.sectec.platform.identity.repository.RolePermissionRepository;
import it.sectec.platform.shared.crud.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
@Transactional
public class RolePermissionService extends CrudService<RolePermission, Request, Response> {
    private final EntityReferences references;

    public RolePermissionService(RolePermissionRepository repository, EntityReferences references) {
        super(repository);
        this.references = references;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_ROLE_PERMISSION_READ')")
    public PageResponse<Response> list(int page, int size) { return super.list(page, size); }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_ROLE_PERMISSION_READ')")
    public Response get(UUID id) { return super.get(id); }

    @Override
    @PreAuthorize("hasAuthority('PERM_ROLE_PERMISSION_WRITE')")
    public Response create(Request request) { return super.create(request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_ROLE_PERMISSION_WRITE')")
    public Response update(UUID id, Request request) { return super.update(id, request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_ROLE_PERMISSION_WRITE')")
    public void delete(UUID id) { super.delete(id); }

    @Override
    protected RolePermission createEntity(Request request) {
        return new RolePermission(references.find(Role.class, request.roleId()),
                references.find(Permission.class, request.permissionId()));
    }

    @Override
    protected void updateEntity(RolePermission entity, Request request) {
        entity.setRole(references.find(Role.class, request.roleId()));
        entity.setPermission(references.find(Permission.class, request.permissionId()));
    }

    @Override
    protected Response response(RolePermission entity) {
        return new Response(entity.getId(), entity.getRole() == null ? null : entity.getRole().getId(),
                entity.getPermission() == null ? null : entity.getPermission().getId());
    }
}

