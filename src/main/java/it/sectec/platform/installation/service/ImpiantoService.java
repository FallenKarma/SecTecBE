package it.sectec.platform.installation.service;

import it.sectec.platform.installation.entity.Impianto;
import it.sectec.platform.installation.entity.TipoImpianto;
import it.sectec.platform.installation.dto.ImpiantoDto.*;
import it.sectec.platform.installation.repository.ImpiantoRepository;
import it.sectec.platform.shared.crud.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
@Transactional
public class ImpiantoService extends CrudService<Impianto, Request, Response> {
    private final EntityReferences references;

    public ImpiantoService(ImpiantoRepository repository, EntityReferences references) {
        super(repository);
        this.references = references;
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
    @PreAuthorize("hasAuthority('PERM_IMPIANTO_WRITE')")
    public Response create(Request request) { return super.create(request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_IMPIANTO_WRITE')")
    public Response update(UUID id, Request request) { return super.update(id, request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_IMPIANTO_WRITE')")
    public void delete(UUID id) { super.delete(id); }

    @Override
    protected Impianto createEntity(Request request) {
        return new Impianto(references.find(TipoImpianto.class, request.tipoImpiantoId()),
                request.indirizzo(),
                request.cap(),
                request.nome(),
                request.descrizione(),
                request.numeroSim(),
                request.codiceSim(),
                request.longitudine(),
                request.latitudine(),
                request.comune());
    }

    @Override
    protected void updateEntity(Impianto entity, Request request) {
        entity.setTipoImpianto(references.find(TipoImpianto.class, request.tipoImpiantoId()));
        entity.setIndirizzo(request.indirizzo());
        entity.setCap(request.cap());
        entity.setNome(request.nome());
        entity.setDescrizione(request.descrizione());
        entity.setNumeroSim(request.numeroSim());
        entity.setCodiceSim(request.codiceSim());
        entity.setLongitudine(request.longitudine());
        entity.setLatitudine(request.latitudine());
        entity.setComune(request.comune());
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

