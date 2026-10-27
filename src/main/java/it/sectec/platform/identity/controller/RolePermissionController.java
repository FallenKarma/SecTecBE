package it.sectec.platform.identity.controller;

import it.sectec.platform.identity.dto.RolePermissionDto.*;
import it.sectec.platform.identity.entity.RolePermission;
import it.sectec.platform.identity.service.RolePermissionService;
import it.sectec.platform.shared.crud.CrudController;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/role-permissions")
public class RolePermissionController extends CrudController<RolePermission, Request, Response> {
    public RolePermissionController(RolePermissionService service) { super(service); }
    @Override
    protected UUID id(Response response) { return response.id(); }
}

