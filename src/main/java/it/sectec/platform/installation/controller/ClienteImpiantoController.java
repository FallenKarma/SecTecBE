package it.sectec.platform.installation.controller;

import it.sectec.platform.installation.dto.ClienteImpiantoDto.*;
import it.sectec.platform.installation.entity.ClienteImpianto;
import it.sectec.platform.installation.service.ClienteImpiantoService;
import it.sectec.platform.shared.crud.CrudController;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clienti-impianti")
public class ClienteImpiantoController extends CrudController<ClienteImpianto, Request, Response> {
    public ClienteImpiantoController(ClienteImpiantoService service) { super(service); }
    @Override
    protected UUID id(Response response) { return response.id(); }
}

