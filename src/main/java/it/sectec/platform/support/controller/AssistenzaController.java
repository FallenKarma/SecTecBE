package it.sectec.platform.support.controller;

import it.sectec.platform.support.dto.AssistenzaDto.*;
import it.sectec.platform.support.entity.Assistenza;
import it.sectec.platform.support.service.AssistenzaService;
import it.sectec.platform.shared.crud.CrudController;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/assistenze")
public class AssistenzaController extends CrudController<Assistenza, Request, Response> {
    public AssistenzaController(AssistenzaService service) { super(service); }
    @Override
    protected UUID id(Response response) { return response.id(); }
}

