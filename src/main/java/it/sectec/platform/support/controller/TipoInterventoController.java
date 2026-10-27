package it.sectec.platform.support.controller;

import it.sectec.platform.support.dto.TipoInterventoDto.*;
import it.sectec.platform.support.entity.TipoIntervento;
import it.sectec.platform.support.service.TipoInterventoService;
import it.sectec.platform.shared.crud.CrudController;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tipi-intervento")
public class TipoInterventoController extends CrudController<TipoIntervento, Request, Response> {
    public TipoInterventoController(TipoInterventoService service) { super(service); }
    @Override
    protected UUID id(Response response) { return response.id(); }
}

