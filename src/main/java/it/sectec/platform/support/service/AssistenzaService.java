package it.sectec.platform.support.service;

import it.sectec.platform.support.entity.Assistenza;
import it.sectec.platform.installation.entity.Impianto;
import it.sectec.platform.support.entity.TipoAssistenza;
import it.sectec.platform.support.dto.AssistenzaDto.*;
import it.sectec.platform.support.repository.AssistenzaRepository;
import it.sectec.platform.shared.crud.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
@Transactional
public class AssistenzaService extends CrudService<Assistenza, Request, Response> {
    private final EntityReferences references;

    public AssistenzaService(AssistenzaRepository repository, EntityReferences references) {
        super(repository);
        this.references = references;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_ASSISTENZA_READ')")
    public PageResponse<Response> list(int page, int size) { return super.list(page, size); }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_ASSISTENZA_READ')")
    public Response get(UUID id) { return super.get(id); }

    @Override
    @PreAuthorize("hasAuthority('PERM_ASSISTENZA_WRITE')")
    public Response create(Request request) { return super.create(request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_ASSISTENZA_WRITE')")
    public Response update(UUID id, Request request) { return super.update(id, request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_ASSISTENZA_WRITE')")
    public void delete(UUID id) { super.delete(id); }

    @Override
    protected Assistenza createEntity(Request request) {
        if (!request.dataFine().isAfter(request.dataInizio())) throw new CrudException(400, "INVALID_DATE_RANGE");
        return new Assistenza(references.find(Impianto.class, request.impiantoId()),
                references.find(TipoAssistenza.class, request.tipoAssistenzaId()),
                request.dataInizio(),
                request.dataFine(),
                request.note());
    }

    @Override
    protected void updateEntity(Assistenza entity, Request request) {
        if (!request.dataFine().isAfter(request.dataInizio())) throw new CrudException(400, "INVALID_DATE_RANGE");
        entity.setImpianto(references.find(Impianto.class, request.impiantoId()));
        entity.setTipoAssistenza(references.find(TipoAssistenza.class, request.tipoAssistenzaId()));
        entity.setDataInizio(request.dataInizio());
        entity.setDataFine(request.dataFine());
        entity.setNote(request.note());
    }

    @Override
    protected Response response(Assistenza entity) {
        return new Response(entity.getId(), entity.getImpianto() == null ? null : entity.getImpianto().getId(),
                entity.getTipoAssistenza() == null ? null : entity.getTipoAssistenza().getId(),
                entity.getDataInizio(),
                entity.getDataFine(),
                entity.getNote());
    }
}

