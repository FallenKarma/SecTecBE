package it.sectec.platform.catalog.service;

import it.sectec.platform.catalog.entity.TipoArticolo;
import it.sectec.platform.catalog.entity.Categoria;
import it.sectec.platform.catalog.dto.TipoArticoloDto.*;
import it.sectec.platform.catalog.repository.TipoArticoloRepository;
import it.sectec.platform.shared.crud.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
@Transactional
public class TipoArticoloService extends CrudService<TipoArticolo, Request, Response> {
    private final EntityReferences references;

    public TipoArticoloService(TipoArticoloRepository repository, EntityReferences references) {
        super(repository);
        this.references = references;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_TIPO_ARTICOLO_READ')")
    public PageResponse<Response> list(int page, int size) { return super.list(page, size); }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_TIPO_ARTICOLO_READ')")
    public Response get(UUID id) { return super.get(id); }

    @Override
    @PreAuthorize("hasAuthority('PERM_TIPO_ARTICOLO_WRITE')")
    public Response create(Request request) { return super.create(request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_TIPO_ARTICOLO_WRITE')")
    public Response update(UUID id, Request request) { return super.update(id, request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_TIPO_ARTICOLO_WRITE')")
    public void delete(UUID id) { super.delete(id); }

    @Override
    protected TipoArticolo createEntity(Request request) {
        return new TipoArticolo(references.find(Categoria.class, request.categoriaId()),
                request.codiceArticolo(),
                request.descrizione(),
                request.marca(),
                request.modello(),
                request.costo());
    }

    @Override
    protected void updateEntity(TipoArticolo entity, Request request) {
        entity.setCategoria(references.find(Categoria.class, request.categoriaId()));
        entity.setCodiceArticolo(request.codiceArticolo());
        entity.setDescrizione(request.descrizione());
        entity.setMarca(request.marca());
        entity.setModello(request.modello());
        entity.setCosto(request.costo());
    }

    @Override
    protected Response response(TipoArticolo entity) {
        return new Response(entity.getId(), entity.getCategoria() == null ? null : entity.getCategoria().getId(),
                entity.getCodiceArticolo(),
                entity.getDescrizione(),
                entity.getMarca(),
                entity.getModello(),
                entity.getCosto());
    }
}

