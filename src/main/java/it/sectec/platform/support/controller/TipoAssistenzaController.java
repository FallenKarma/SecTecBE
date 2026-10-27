package it.sectec.platform.support.controller;

import it.sectec.platform.support.dto.TipoAssistenzaDto.*;
import it.sectec.platform.support.entity.TipoAssistenza;
import it.sectec.platform.support.service.TipoAssistenzaService;
import it.sectec.platform.shared.crud.CrudController;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tipi-assistenza")
public class TipoAssistenzaController extends CrudController<TipoAssistenza, Request, Response> {
    public TipoAssistenzaController(TipoAssistenzaService service) { super(service); }
    @Override
    protected UUID id(Response response) { return response.id(); }
}

