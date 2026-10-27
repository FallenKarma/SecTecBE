package it.sectec.platform.identity.controller;

import it.sectec.platform.identity.dto.PermissionDto.*;
import it.sectec.platform.identity.entity.Permission;
import it.sectec.platform.identity.service.PermissionService;
import it.sectec.platform.shared.crud.CrudController;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController extends CrudController<Permission, Request, Response> {
    public PermissionController(PermissionService service) { super(service); }
    @Override
    protected UUID id(Response response) { return response.id(); }
}

