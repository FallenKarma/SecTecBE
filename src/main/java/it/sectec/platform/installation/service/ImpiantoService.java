package it.sectec.platform.installation.service;

import it.sectec.platform.customer.repository.ClienteRepository;
import it.sectec.platform.installation.dto.ImpiantoDto.*;
import it.sectec.platform.installation.entity.Impianto;
import it.sectec.platform.installation.entity.TipoImpianto;
import it.sectec.platform.installation.repository.ClienteImpiantoRepository;
import it.sectec.platform.installation.repository.ImpiantoRepository;
import it.sectec.platform.shared.crud.CrudException;
import it.sectec.platform.shared.crud.CrudService;
import it.sectec.platform.shared.crud.EntityReferences;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ImpiantoService extends CrudService<Impianto, Request, Response> {
    private final EntityReferences references;
    private final GoogleGeocodingService geocoding;
    private final ClienteImpiantoRepository associations;
    private final ImpiantoRepository impianti;
    private final ClienteRepository clienti;

    public ImpiantoService(ImpiantoRepository repository, EntityReferences references,
            ClienteRepository clienti,
            GoogleGeocodingService geocoding,
            ClienteImpiantoRepository associations) {
        super(repository);
        this.references = references;
        this.impianti = repository;
        this.clienti = clienti;
        this.geocoding = geocoding;
        this.associations = associations;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_CLIENTE_READ') and hasAuthority('PERM_IMPIANTO_READ')")
    public java.util.List<Response> findByCliente(UUID clienteId) {
        log.debug("Client installations lookup requested");
        if (!clienti.existsById(clienteId)) throw new CrudException(404, "NOT_FOUND");
        var result = impianti.findAllByClienteId(clienteId).stream().map(this::response).toList();
        log.debug("Client installations lookup completed: count={}", result.size());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_IMPIANTO_READ')")
    public PageResponse<Response> list(int page, int size) { return super.list(page, size); }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_IMPIANTO_READ')")
    public Response get(UUID id) { return super.get(id); }

    @Override
    @PreAuthorize("hasAuthority('PERM_IMPIANTO_WRITE') and (#request.clientiIds() == null or #request.clientiIds().isEmpty() or (hasAuthority('PERM_CLIENTE_READ') and hasAuthority('PERM_CLIENTE_IMPIANTO_WRITE')))")
    public Response create(Request request) {
        var selected = request.clientiIds() == null ? java.util.List.<UUID>of() : request.clientiIds().stream().distinct().toList();
        var clients = selected.stream().map(id -> references.find(it.sectec.platform.customer.entity.Cliente.class, id)).toList();
        var entity = impianti.saveAndFlush(createEntity(request));
        associations.saveAllAndFlush(clients.stream().map(client ->
                new it.sectec.platform.installation.entity.ClienteImpianto(client, entity)).toList());
        return response(entity);
    }

    @Override
    @PreAuthorize("hasAuthority('PERM_IMPIANTO_WRITE')")
    public Response update(UUID id, Request request) {
        // Associations in this request are supported only during creation.
        if (request.clientiIds() != null && !request.clientiIds().isEmpty()) throw new CrudException(400, "INVALID_REQUEST");
        return super.update(id, request);
    }

    @Override
    @PreAuthorize("hasAuthority('PERM_IMPIANTO_WRITE')")
    public void delete(UUID id) {
        if (!impianti.existsById(id)) throw new CrudException(404, "NOT_FOUND");
        associations.deleteAllByImpiantoId(id);
        associations.flush();
        super.delete(id);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_IMPIANTO_READ') and hasAuthority('PERM_CLIENTE_READ') and hasAuthority('PERM_CLIENTE_IMPIANTO_READ')")
    public java.util.List<AssociatedClient> associatedClients(UUID id) {
        if (!impianti.existsById(id)) throw new CrudException(404, "NOT_FOUND");
        return clientResponses(id);
    }

    @PreAuthorize("hasAuthority('PERM_IMPIANTO_READ') and hasAuthority('PERM_CLIENTE_READ') and hasAuthority('PERM_CLIENTE_IMPIANTO_READ') and hasAuthority('PERM_CLIENTE_IMPIANTO_WRITE')")
    public java.util.List<AssociatedClient> updateClients(UUID id, ClientsRequest request) {
        var installation = impianti.findById(id).orElseThrow(() -> new CrudException(404, "NOT_FOUND"));
        var selected = request.clientiIds().stream().distinct().toList();
        var clients = selected.stream().map(clientId -> references.find(it.sectec.platform.customer.entity.Cliente.class, clientId)).toList();
        var existing = associations.findForInstallation(id);
        var existingIds = existing.stream().map(link -> link.getCliente().getId()).collect(java.util.stream.Collectors.toSet());
        associations.deleteAll(existing.stream().filter(link -> !selected.contains(link.getCliente().getId())).toList());
        associations.saveAllAndFlush(clients.stream().filter(client -> !existingIds.contains(client.getId()))
                .map(client -> new it.sectec.platform.installation.entity.ClienteImpianto(client, installation)).toList());
        return clientResponses(id);
    }

    private java.util.List<AssociatedClient> clientResponses(UUID id) {
        return associations.findForInstallation(id).stream().map(link -> {
            var client = link.getCliente();
            return new AssociatedClient(client.getId(), client.getNome(), client.getCognome(), client.getCodiceFiscale(), client.getIndirizzo());
        }).toList();
    }

    @Override
    protected Impianto createEntity(Request request) {
        var entity = new Impianto(references.find(TipoImpianto.class, request.tipoImpiantoId()),
                request.indirizzo(),
                request.cap(),
                request.nome(),
                request.descrizione(),
                request.numeroSim(),
                request.codiceSim(),
                null,
                null,
                request.comune());
        geocode(entity);
        return entity;
    }

    @Override
    protected void updateEntity(Impianto entity, Request request) {
        boolean addressChanged = !java.util.Objects.equals(entity.getIndirizzo(), request.indirizzo())
                || !java.util.Objects.equals(entity.getCap(), request.cap())
                || !java.util.Objects.equals(entity.getComune(), request.comune());
        entity.setTipoImpianto(references.find(TipoImpianto.class, request.tipoImpiantoId()));
        entity.setIndirizzo(request.indirizzo());
        entity.setCap(request.cap());
        entity.setNome(request.nome());
        entity.setDescrizione(request.descrizione());
        entity.setNumeroSim(request.numeroSim());
        entity.setCodiceSim(request.codiceSim());
        entity.setComune(request.comune());
        if (addressChanged) geocode(entity);
    }

    private void geocode(Impianto entity) {
        var coordinates = geocoding.locate(entity.getIndirizzo(), entity.getCap(), entity.getComune());
        entity.setLatitudine(coordinates.map(GoogleGeocodingService.Coordinates::latitude).orElse(null));
        entity.setLongitudine(coordinates.map(GoogleGeocodingService.Coordinates::longitude).orElse(null));
    }

    @PreAuthorize("hasAuthority('PERM_IMPIANTO_WRITE')")
    public Response recalculate(UUID id) {
        var entity = impianti.findById(id).orElseThrow(() -> new CrudException(404, "NOT_FOUND"));
        geocode(entity);
        return response(impianti.saveAndFlush(entity));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_IMPIANTO_READ') or hasRole('Admin')")
    public java.util.List<MapPoint> map() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        var permissions = auth.getAuthorities().stream().map(a -> a.getAuthority()).collect(java.util.stream.Collectors.toSet());
        boolean readable = permissions.contains("PERM_CLIENTE_READ") ;
        var names = new java.util.HashMap<UUID, java.util.List<String>>();
        if (readable) {
            for (var association : associations.findAllWithClient()) {
                var client = association.getCliente();
                names.computeIfAbsent(association.getImpianto().getId(), ignored -> new java.util.ArrayList<>()).add(client.getNome() + " " + client.getCognome());
            }
            names.values().forEach(list -> list.sort(String.CASE_INSENSITIVE_ORDER));
        }
        return impianti.findAll(org.springframework.data.domain.Sort.by("id")).stream()
                .map(entity -> new MapPoint(entity.getId(), entity.getNome(), entity.getLatitudine(), entity.getLongitudine(),
                        readable ? names.getOrDefault(entity.getId(), java.util.List.of()) : null)).toList();
    }

    @Override
    protected Response response(Impianto entity) {
        return new Response(entity.getId(), entity.getTipoImpianto() == null ? null : entity.getTipoImpianto().getId(),
                entity.getIndirizzo(),
                entity.getCap(),
                entity.getNome(),
                entity.getDescrizione(),
                entity.getNumeroSim(),
                entity.getCodiceSim(),
                entity.getLongitudine(),
                entity.getLatitudine(),
                entity.getComune());
    }
}

