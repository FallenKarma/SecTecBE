package it.sectec.platform.identity.controller;

import it.sectec.platform.identity.dto.RoleDto.*;
import it.sectec.platform.identity.entity.Role;
import it.sectec.platform.identity.service.RoleService;
import it.sectec.platform.shared.crud.CrudController;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController extends CrudController<Role, Request, Response> {
    public RoleController(RoleService service) { super(service); }
    @Override
    protected UUID id(Response response) { return response.id(); }
}

