package it.sectec.platform.installation.controller;

import it.sectec.platform.installation.dto.ImpiantoDto.*;
import it.sectec.platform.installation.entity.Impianto;
import it.sectec.platform.installation.service.ImpiantoService;
import it.sectec.platform.shared.crud.CrudController;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/impianti")
public class ImpiantoController extends CrudController<Impianto, Request, Response> {
    public ImpiantoController(ImpiantoService service) { super(service); }
    @Override
    protected UUID id(Response response) { return response.id(); }
}

