package it.sectec.platform.shared.crud;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public abstract class CrudController<E, Q, R> {
    private final CrudService<E, Q, R> service;
    protected CrudController(CrudService<E, Q, R> service) { this.service = service; }
    protected abstract UUID id(R response);
    @GetMapping
    public CrudService.PageResponse<R> list(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return service.list(page, size);
    }
    @GetMapping("/{id}")
    public R get(@PathVariable UUID id) { return service.get(id); }
    @PostMapping
    public ResponseEntity<R> create(@Valid @RequestBody Q request) {
        R result = service.create(request);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(id(result)).toUri()).body(result);
    }
    @PutMapping("/{id}")
    public R update(@PathVariable UUID id, @Valid @RequestBody Q request) { return service.update(id, request); }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
