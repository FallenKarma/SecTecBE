package it.sectec.platform.customer.controller;

import it.sectec.platform.customer.dto.ClienteDto.*;
import it.sectec.platform.customer.entity.Cliente;
import it.sectec.platform.customer.service.ClienteService;
import it.sectec.platform.shared.crud.CrudController;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clienti")
public class ClienteController extends CrudController<Cliente, Request, Response> {
    public ClienteController(ClienteService service) { super(service); }
    @Override
    protected UUID id(Response response) { return response.id(); }
}

