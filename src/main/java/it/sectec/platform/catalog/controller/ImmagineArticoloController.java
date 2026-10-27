package it.sectec.platform.catalog.controller;

import it.sectec.platform.catalog.dto.ImmagineArticoloDto.*;
import it.sectec.platform.catalog.entity.ImmagineArticolo;
import it.sectec.platform.catalog.service.ImmagineArticoloService;
import it.sectec.platform.shared.crud.CrudController;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/immagini-articolo")
public class ImmagineArticoloController extends CrudController<ImmagineArticolo, Request, Response> {
    public ImmagineArticoloController(ImmagineArticoloService service) { super(service); }
    @Override
    protected UUID id(Response response) { return response.id(); }
}

