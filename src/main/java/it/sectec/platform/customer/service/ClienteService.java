package it.sectec.platform.customer.service;

import it.sectec.platform.customer.entity.Cliente;
import it.sectec.platform.identity.entity.User;
import it.sectec.platform.customer.dto.ClienteDto.*;
import it.sectec.platform.customer.repository.ClienteRepository;
import it.sectec.platform.shared.crud.*;
import java.util.UUID;
import java.util.Locale;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
@Transactional
public class ClienteService extends CrudService<Cliente, Request, Response> {
    private final EntityReferences references;
    private final ClienteRepository clienti;

    public ClienteService(ClienteRepository repository, EntityReferences references) {
        super(repository);
        this.references = references;
        this.clienti = repository;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_CLIENTE_READ')")
    public PageResponse<Response> list(int page, int size) { return super.list(page, size); }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_CLIENTE_READ')")
    public PageResponse<Response> search(String nome, String cognome, String codiceFiscale, int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new CrudException(400, "INVALID_PAGINATION");
        String normalizedNome = normalizeFilter(nome, 100);
        String normalizedCognome = normalizeFilter(cognome, 100);
        String normalizedCodiceFiscale = normalizeFilter(codiceFiscale, 16);
        log.debug("Client search: nomeFilter={} cognomeFilter={} codiceFiscaleFilter={} page={} size={}",
                normalizedNome != null, normalizedCognome != null, normalizedCodiceFiscale != null, page, size);
        Specification<Cliente> filters = (root, query, cb) -> cb.conjunction();
        if (normalizedNome != null) filters = filters.and(contains("nome", normalizedNome));
        if (normalizedCognome != null) filters = filters.and(contains("cognome", normalizedCognome));
        if (normalizedCodiceFiscale != null) filters = filters.and((root, query, cb) ->
                cb.equal(cb.lower(cb.trim(root.<String>get("codiceFiscale"))), normalizedCodiceFiscale));
        var result = clienti.findAll(filters, PageRequest.of(page, size, Sort.by("id")));
        log.debug("Client search completed: totalElements={}", result.getTotalElements());
        return new PageResponse<>(result.getContent().stream().map(this::response).toList(),
                page, size, result.getTotalElements(), result.getTotalPages());
    }

    private static String normalizeFilter(String value, int maxLength) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        if (normalized.length() > maxLength) throw new CrudException(400, "INVALID_REQUEST");
        return normalized.toLowerCase(Locale.ROOT);
    }

    private static Specification<Cliente> contains(String field, String value) {
        // Treat SQL LIKE wildcards as literal input, including the escape character itself.
        String pattern = "%" + value.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.<String>get(field)), pattern, '!');
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_CLIENTE_READ')")
    public Response get(UUID id) { return super.get(id); }

    @Override
    @PreAuthorize("hasAuthority('PERM_CLIENTE_WRITE')")
    public Response create(Request request) { return super.create(request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_CLIENTE_WRITE')")
    public Response update(UUID id, Request request) { return super.update(id, request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_CLIENTE_WRITE')")
    public void delete(UUID id) { super.delete(id); }

    @Override
    protected Cliente createEntity(Request request) {
        return new Cliente(references.find(User.class, request.userId()),
                request.indirizzo(),
                request.cap(),
                request.codiceFiscale(),
                request.partitaIva(),
                request.pec(),
                request.dataNascita(),
                request.nome(),
                request.cognome(),
                request.iban(),
                request.indirizzoFatturazione());
    }

    @Override
    protected void updateEntity(Cliente entity, Request request) {
        entity.setUser(references.find(User.class, request.userId()));
        entity.setIndirizzo(request.indirizzo());
        entity.setCap(request.cap());
        entity.setCodiceFiscale(request.codiceFiscale());
        entity.setPartitaIva(request.partitaIva());
        entity.setPec(request.pec());
        entity.setDataNascita(request.dataNascita());
        entity.setNome(request.nome());
        entity.setCognome(request.cognome());
        entity.setIban(request.iban());
        entity.setIndirizzoFatturazione(request.indirizzoFatturazione());
    }

    @Override
    protected Response response(Cliente entity) {
        return new Response(entity.getId(), entity.getUser() == null ? null : entity.getUser().getId(),
                entity.getIndirizzo(),
                entity.getCap(),
                entity.getCodiceFiscale(),
                entity.getPartitaIva(),
                entity.getPec(),
                entity.getDataNascita(),
                entity.getNome(),
                entity.getCognome(),
                entity.getIban(),
                entity.getIndirizzoFatturazione());
    }
}

