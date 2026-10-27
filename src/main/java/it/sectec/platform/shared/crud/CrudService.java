package it.sectec.platform.shared.crud;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public abstract class CrudService<E, Q, R> {
    protected final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(getClass());
    private final JpaRepository<E, UUID> repository;
    protected CrudService(JpaRepository<E, UUID> repository) { this.repository = repository; }
    protected abstract E createEntity(Q request);
    protected abstract void updateEntity(E entity, Q request);
    protected abstract R response(E entity);

    public PageResponse<R> list(int page, int size) {
        log.debug("CRUD list requested: page={} size={}", page, size);
        if (page < 0 || size < 1 || size > 100) throw new CrudException(400, "INVALID_PAGINATION");
        var result = repository.findAll(PageRequest.of(page, size, Sort.by("id")));
        return new PageResponse<>(result.getContent().stream().map(this::response).toList(),
                page, size, result.getTotalElements(), result.getTotalPages());
    }
    public R get(UUID id) {
        log.debug("CRUD get requested");
        return response(required(id));
    }
    public R create(Q request) {
        log.debug("CRUD create requested");
        R result = response(repository.saveAndFlush(createEntity(request)));
        it.sectec.platform.shared.TransactionLog.committed(log, "CRUD create committed");
        return result;
    }
    public R update(UUID id, Q request) {
        log.debug("CRUD update requested");
        var entity = required(id);
        updateEntity(entity, request);
        R result = response(repository.saveAndFlush(entity));
        it.sectec.platform.shared.TransactionLog.committed(log, "CRUD update committed");
        return result;
    }
    public void delete(UUID id) {
        log.debug("CRUD delete requested");
        repository.delete(required(id));
        repository.flush();
        it.sectec.platform.shared.TransactionLog.committed(log, "CRUD delete committed");
    }
    private E required(UUID id) {
        return repository.findById(id).orElseThrow(() -> new CrudException(404, "NOT_FOUND"));
    }
    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {}
}
