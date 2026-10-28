package it.sectec.platform.identity.service;

import it.sectec.platform.identity.dto.UserRoleDto.Request;
import it.sectec.platform.identity.dto.UserRoleDto.Response;
import it.sectec.platform.identity.entity.Role;
import it.sectec.platform.identity.entity.User;
import it.sectec.platform.identity.entity.UserRole;
import it.sectec.platform.identity.repository.UserRoleRepository;
import it.sectec.platform.shared.crud.CrudService;
import it.sectec.platform.shared.crud.EntityReferences;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UserRoleService extends CrudService<UserRole, Request, Response> {
    private final EntityReferences references;

    public UserRoleService(UserRoleRepository repository, EntityReferences references) {
        super(repository);
        this.references = references;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_USER_ROLE_READ')")
    public PageResponse<Response> list(int page, int size) { return super.list(page, size); }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_USER_ROLE_READ')")
    public Response get(UUID id) { return super.get(id); }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Response create(Request request) { return super.create(request); }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Response update(UUID id, Request request) { return super.update(id, request); }

    @Override
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_USER_ROLE_WRITE')")
    public void delete(UUID id) { super.delete(id); }

    @Override
    protected UserRole createEntity(Request request) {
        return new UserRole(references.find(User.class, request.userId()),
                references.find(Role.class, request.roleId()),
                request.assignedAt());
    }

    @Override
    protected void updateEntity(UserRole entity, Request request) {
        entity.setUser(references.find(User.class, request.userId()));
        entity.setRole(references.find(Role.class, request.roleId()));
        entity.setAssignedAt(request.assignedAt());
    }

    @Override
    protected Response response(UserRole entity) {
        return new Response(entity.getId(), entity.getUser() == null ? null : entity.getUser().getId(),
                entity.getRole() == null ? null : entity.getRole().getId(),
                entity.getAssignedAt());
    }
}

