package it.sectec.platform.inventory.controller;

import it.sectec.platform.inventory.dto.BollaDto.*;
import it.sectec.platform.inventory.entity.Bolla;
import it.sectec.platform.inventory.service.BollaService;
import it.sectec.platform.shared.crud.CrudController;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bolle")
public class BollaController extends CrudController<Bolla, Request, Response> {
    public BollaController(BollaService service) { super(service); }
    @Override
    protected UUID id(Response response) { return response.id(); }
}

