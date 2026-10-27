package it.sectec.platform.shared.crud;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public abstract class CrudService<E, Q, R> {
    private final JpaRepository<E, UUID> repository;
    protected CrudService(JpaRepository<E, UUID> repository) { this.repository = repository; }
    protected abstract E createEntity(Q request);
    protected abstract void updateEntity(E entity, Q request);
    protected abstract R response(E entity);

    public PageResponse<R> list(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new CrudException(400, "INVALID_PAGINATION");
        var result = repository.findAll(PageRequest.of(page, size, Sort.by("id")));
        return new PageResponse<>(result.getContent().stream().map(this::response).toList(),
                page, size, result.getTotalElements(), result.getTotalPages());
    }
    public R get(UUID id) { return response(required(id)); }
    public R create(Q request) { return response(repository.saveAndFlush(createEntity(request))); }
    public R update(UUID id, Q request) {
        var entity = required(id);
        updateEntity(entity, request);
        return response(repository.saveAndFlush(entity));
    }
    public void delete(UUID id) {
        repository.delete(required(id));
        repository.flush();
    }
    private E required(UUID id) {
        return repository.findById(id).orElseThrow(() -> new CrudException(404, "NOT_FOUND"));
    }
    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {}
}
