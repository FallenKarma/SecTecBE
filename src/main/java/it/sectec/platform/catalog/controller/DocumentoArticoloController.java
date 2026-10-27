package it.sectec.platform.catalog.controller;

import it.sectec.platform.catalog.dto.DocumentoArticoloDto.*;
import it.sectec.platform.catalog.entity.DocumentoArticolo;
import it.sectec.platform.catalog.service.DocumentoArticoloService;
import it.sectec.platform.shared.crud.CrudController;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/documenti-articolo")
public class DocumentoArticoloController extends CrudController<DocumentoArticolo, Request, Response> {
    public DocumentoArticoloController(DocumentoArticoloService service) { super(service); }
    @Override
    protected UUID id(Response response) { return response.id(); }
}

