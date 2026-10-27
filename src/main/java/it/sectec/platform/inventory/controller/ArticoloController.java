package it.sectec.platform.inventory.controller;

import it.sectec.platform.inventory.dto.ArticoloDto.*;
import it.sectec.platform.inventory.entity.Articolo;
import it.sectec.platform.inventory.service.ArticoloService;
import it.sectec.platform.shared.crud.CrudController;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/articoli")
public class ArticoloController extends CrudController<Articolo, Request, Response> {
    public ArticoloController(ArticoloService service) { super(service); }
    @Override
    protected UUID id(Response response) { return response.id(); }
}

