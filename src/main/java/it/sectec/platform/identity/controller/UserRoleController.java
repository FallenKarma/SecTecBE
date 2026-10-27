package it.sectec.platform.identity.controller;

import it.sectec.platform.identity.dto.UserRoleDto.*;
import it.sectec.platform.identity.entity.UserRole;
import it.sectec.platform.identity.service.UserRoleService;
import it.sectec.platform.shared.crud.CrudController;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user-roles")
public class UserRoleController extends CrudController<UserRole, Request, Response> {
    public UserRoleController(UserRoleService service) { super(service); }
    @Override
    protected UUID id(Response response) { return response.id(); }
}

