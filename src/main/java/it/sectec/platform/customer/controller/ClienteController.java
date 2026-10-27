package it.sectec.platform.customer.controller;

import it.sectec.platform.customer.dto.ClienteDto.*;
import it.sectec.platform.customer.entity.Cliente;
import it.sectec.platform.customer.service.ClienteService;
import it.sectec.platform.shared.crud.CrudController;
import it.sectec.platform.shared.crud.CrudService.PageResponse;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clienti")
public class ClienteController extends CrudController<Cliente, Request, Response> {
    private final ClienteService clienti;
    public ClienteController(ClienteService service) {
        super(service);
        this.clienti = service;
    }

    @GetMapping("/search")
    public PageResponse<Response> search(@RequestParam(required = false) String nome,
            @RequestParam(required = false) String cognome,
            @RequestParam(required = false) String codiceFiscale,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return clienti.search(nome, cognome, codiceFiscale, page, size);
    }
    @Override
    protected UUID id(Response response) { return response.id(); }
}

