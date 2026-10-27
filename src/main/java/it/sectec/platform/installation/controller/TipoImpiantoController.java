package it.sectec.platform.installation.controller;

import it.sectec.platform.installation.dto.TipoImpiantoDto.*;
import it.sectec.platform.installation.entity.TipoImpianto;
import it.sectec.platform.installation.service.TipoImpiantoService;
import it.sectec.platform.shared.crud.CrudController;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tipi-impianto")
public class TipoImpiantoController extends CrudController<TipoImpianto, Request, Response> {
    public TipoImpiantoController(TipoImpiantoService service) { super(service); }
    @Override
    protected UUID id(Response response) { return response.id(); }
}

