package it.sectec.platform.catalog.controller;

import it.sectec.platform.catalog.dto.TipoArticoloDto.*;
import it.sectec.platform.catalog.entity.TipoArticolo;
import it.sectec.platform.catalog.service.TipoArticoloService;
import it.sectec.platform.shared.crud.CrudController;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tipi-articolo")
public class TipoArticoloController extends CrudController<TipoArticolo, Request, Response> {
    public TipoArticoloController(TipoArticoloService service) { super(service); }
    @Override
    protected UUID id(Response response) { return response.id(); }
}

