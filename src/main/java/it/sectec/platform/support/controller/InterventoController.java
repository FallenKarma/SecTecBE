package it.sectec.platform.support.controller;

import it.sectec.platform.support.dto.InterventoDto.*;
import it.sectec.platform.support.entity.Intervento;
import it.sectec.platform.support.service.InterventoService;
import it.sectec.platform.shared.crud.CrudController;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/interventi")
public class InterventoController extends CrudController<Intervento, Request, Response> {
    public InterventoController(InterventoService service) { super(service); }
    @Override
    protected UUID id(Response response) { return response.id(); }
}

