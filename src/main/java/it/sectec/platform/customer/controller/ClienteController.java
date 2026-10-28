package it.sectec.platform.customer.controller;

import it.sectec.platform.customer.dto.ClienteDto.Request;
import it.sectec.platform.customer.dto.ClienteDto.Response;
import it.sectec.platform.customer.entity.Cliente;
import it.sectec.platform.customer.service.ClienteService;
import it.sectec.platform.shared.crud.CrudController;
import it.sectec.platform.shared.crud.CrudService.PageResponse;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clienti")
public class ClienteController extends CrudController<Cliente, Request, Response> {
    private final ClienteService clienti;
    private final it.sectec.platform.installation.service.ImpiantoService impianti;
    public ClienteController(ClienteService service, it.sectec.platform.installation.service.ImpiantoService impianti) {
        super(service);
        this.clienti = service;
        this.impianti = impianti;
    }

    @GetMapping("/{clienteId}/impianti")
    public java.util.List<it.sectec.platform.installation.dto.ImpiantoDto.Response> impianti(@PathVariable UUID clienteId) {
        return impianti.findByCliente(clienteId);
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

