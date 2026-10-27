package it.sectec.platform.inventory.controller;

import it.sectec.platform.inventory.dto.BollaTipoArticoloDto.*;
import it.sectec.platform.inventory.entity.BollaTipoArticolo;
import it.sectec.platform.inventory.service.BollaTipoArticoloService;
import it.sectec.platform.shared.crud.CrudController;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bolle-tipi-articolo")
public class BollaTipoArticoloController extends CrudController<BollaTipoArticolo, Request, Response> {
    public BollaTipoArticoloController(BollaTipoArticoloService service) { super(service); }
    @Override
    protected UUID id(Response response) { return response.id(); }
}

