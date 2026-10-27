package it.sectec.platform.customer.service;

import it.sectec.platform.customer.entity.Cliente;
import it.sectec.platform.identity.entity.User;
import it.sectec.platform.customer.dto.ClienteDto.*;
import it.sectec.platform.customer.repository.ClienteRepository;
import it.sectec.platform.shared.crud.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
@Transactional
public class ClienteService extends CrudService<Cliente, Request, Response> {
    private final EntityReferences references;

    public ClienteService(ClienteRepository repository, EntityReferences references) {
        super(repository);
        this.references = references;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_CLIENTE_READ')")
    public PageResponse<Response> list(int page, int size) { return super.list(page, size); }

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

