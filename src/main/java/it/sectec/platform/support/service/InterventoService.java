package it.sectec.platform.support.service;

import it.sectec.platform.support.entity.Intervento;
import it.sectec.platform.installation.entity.Impianto;
import it.sectec.platform.support.entity.TipoIntervento;
import it.sectec.platform.support.entity.Assistenza;
import it.sectec.platform.support.dto.InterventoDto.*;
import it.sectec.platform.support.repository.InterventoRepository;
import it.sectec.platform.shared.crud.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
@Transactional
public class InterventoService extends CrudService<Intervento, Request, Response> {
    private final EntityReferences references;

    public InterventoService(InterventoRepository repository, EntityReferences references) {
        super(repository);
        this.references = references;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_INTERVENTO_READ')")
    public PageResponse<Response> list(int page, int size) { return super.list(page, size); }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_INTERVENTO_READ')")
    public Response get(UUID id) { return super.get(id); }

    @Override
    @PreAuthorize("hasAuthority('PERM_INTERVENTO_WRITE')")
    public Response create(Request request) { return super.create(request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_INTERVENTO_WRITE')")
    public Response update(UUID id, Request request) { return super.update(id, request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_INTERVENTO_WRITE')")
    public void delete(UUID id) { super.delete(id); }

    @Override
    protected Intervento createEntity(Request request) {
        var assistance = references.find(Assistenza.class, request.assistenzaId());
        if (assistance != null && (assistance.getArticolo().getImpianto() == null ||
                !assistance.getArticolo().getImpianto().getId().equals(request.impiantoId()))) {
            throw new CrudException(400, "INCONSISTENT_ASSISTANCE");
        }
        return new Intervento(references.find(Impianto.class, request.impiantoId()),
                references.find(TipoIntervento.class, request.tipoInterventoId()),
                references.find(Assistenza.class, request.assistenzaId()),
                request.data(),
                request.note(),
                request.esito());
    }

    @Override
    protected void updateEntity(Intervento entity, Request request) {
        var assistance = references.find(Assistenza.class, request.assistenzaId());
        if (assistance != null && (assistance.getArticolo().getImpianto() == null ||
                !assistance.getArticolo().getImpianto().getId().equals(request.impiantoId()))) {
            throw new CrudException(400, "INCONSISTENT_ASSISTANCE");
        }
        entity.setImpianto(references.find(Impianto.class, request.impiantoId()));
        entity.setTipoIntervento(references.find(TipoIntervento.class, request.tipoInterventoId()));
        entity.setAssistenza(references.find(Assistenza.class, request.assistenzaId()));
        entity.setData(request.data());
        entity.setNote(request.note());
        entity.setEsito(request.esito());
    }

    @Override
    protected Response response(Intervento entity) {
        return new Response(entity.getId(), entity.getImpianto() == null ? null : entity.getImpianto().getId(),
                entity.getTipoIntervento() == null ? null : entity.getTipoIntervento().getId(),
                entity.getAssistenza() == null ? null : entity.getAssistenza().getId(),
                entity.getData(),
                entity.getNote(),
                entity.getEsito());
    }
}

