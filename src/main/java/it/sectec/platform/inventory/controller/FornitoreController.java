package it.sectec.platform.inventory.controller;

import it.sectec.platform.inventory.dto.FornitoreDto.*;
import it.sectec.platform.inventory.entity.Fornitore;
import it.sectec.platform.inventory.service.FornitoreService;
import it.sectec.platform.shared.crud.CrudController;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fornitori")
public class FornitoreController extends CrudController<Fornitore, Request, Response> {
    public FornitoreController(FornitoreService service) { super(service); }
    @Override
    protected UUID id(Response response) { return response.id(); }
}

