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
    private final ImpiantoService impianti;
    public ImpiantoController(ImpiantoService service) { super(service); this.impianti = service; }
    @GetMapping("/mappa")
    public java.util.List<MapPoint> map() { return impianti.map(); }
    @PostMapping("/{id}/coordinate/ricalcola")
    public Response recalculate(@PathVariable UUID id) { return impianti.recalculate(id); }
    @GetMapping("/{id}/clienti")
    public java.util.List<AssociatedClient> clients(@PathVariable UUID id) { return impianti.associatedClients(id); }
    @PutMapping("/{id}/clienti")
    public java.util.List<AssociatedClient> updateClients(@PathVariable UUID id, @jakarta.validation.Valid @RequestBody ClientsRequest request) {
        return impianti.updateClients(id, request);
    }
    @Override
    protected UUID id(Response response) { return response.id(); }
}

