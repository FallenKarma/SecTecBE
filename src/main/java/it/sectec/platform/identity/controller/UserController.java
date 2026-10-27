package it.sectec.platform.identity.controller;

import it.sectec.platform.identity.dto.UserDto.*;
import it.sectec.platform.identity.entity.User;
import it.sectec.platform.identity.service.UserService;
import it.sectec.platform.shared.crud.CrudController;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController extends CrudController<User, Request, Response> {
    public UserController(UserService service) { super(service); }
    @Override
    protected UUID id(Response response) { return response.id(); }
}

