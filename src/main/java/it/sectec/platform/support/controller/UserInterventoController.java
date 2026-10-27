package it.sectec.platform.support.controller;

import it.sectec.platform.support.dto.UserInterventoDto.*;
import it.sectec.platform.support.entity.UserIntervento;
import it.sectec.platform.support.service.UserInterventoService;
import it.sectec.platform.shared.crud.CrudController;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user-interventi")
public class UserInterventoController extends CrudController<UserIntervento, Request, Response> {
    public UserInterventoController(UserInterventoService service) { super(service); }
    @Override
    protected UUID id(Response response) { return response.id(); }
}

