package it.sectec.platform.catalog.service;

import it.sectec.platform.catalog.entity.Categoria;

import it.sectec.platform.catalog.dto.CategoriaDto.*;
import it.sectec.platform.catalog.repository.CategoriaRepository;
import it.sectec.platform.shared.crud.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
@Transactional
public class CategoriaService extends CrudService<Categoria, Request, Response> {
    private final EntityReferences references;

    public CategoriaService(CategoriaRepository repository, EntityReferences references) {
        super(repository);
        this.references = references;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_CATEGORIA_READ')")
    public PageResponse<Response> list(int page, int size) { return super.list(page, size); }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_CATEGORIA_READ')")
    public Response get(UUID id) { return super.get(id); }

    @Override
    @PreAuthorize("hasAuthority('PERM_CATEGORIA_WRITE')")
    public Response create(Request request) { return super.create(request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_CATEGORIA_WRITE')")
    public Response update(UUID id, Request request) { return super.update(id, request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_CATEGORIA_WRITE')")
    public void delete(UUID id) { super.delete(id); }

    @Override
    protected Categoria createEntity(Request request) {
        return new Categoria(request.nome(),
                request.descrizione());
    }

    @Override
    protected void updateEntity(Categoria entity, Request request) {
        entity.setNome(request.nome());
        entity.setDescrizione(request.descrizione());
    }

    @Override
    protected Response response(Categoria entity) {
        return new Response(entity.getId(), entity.getNome(),
                entity.getDescrizione());
    }
}

