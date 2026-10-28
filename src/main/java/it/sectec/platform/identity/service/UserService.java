package it.sectec.platform.identity.service;

import it.sectec.platform.identity.entity.User;

import it.sectec.platform.identity.dto.UserDto.*;
import it.sectec.platform.identity.repository.UserRepository;
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
public class UserService extends CrudService<User, Request, Response> {
    private final EntityReferences references;
    private final UserRepository users;
    private final org.springframework.security.crypto.password.PasswordEncoder encoder;

    public UserService(UserRepository repository, EntityReferences references, org.springframework.security.crypto.password.PasswordEncoder encoder) {
        super(repository);
        this.references = references;
        this.users = repository;
        this.encoder = encoder;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_USER_READ')")
    public PageResponse<Response> list(int page, int size) { return super.list(page, size); }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_USER_READ')")
    public PageResponse<Response> search(String nome, String cognome, int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new CrudException(400, "INVALID_PAGINATION");
        String normalizedNome = normalizeFilter(nome);
        String normalizedCognome = normalizeFilter(cognome);
        log.debug("User search: nomeFilter={} cognomeFilter={} page={} size={}",
                normalizedNome != null, normalizedCognome != null, page, size);
        Specification<User> filters = (root, query, cb) -> cb.conjunction();
        if (normalizedNome != null) filters = filters.and(contains("nome", normalizedNome));
        if (normalizedCognome != null) filters = filters.and(contains("cognome", normalizedCognome));
        var result = users.findAll(filters, PageRequest.of(page, size, Sort.by("id")));
        log.debug("User search completed: totalElements={}", result.getTotalElements());
        return new PageResponse<>(result.getContent().stream().map(this::response).toList(),
                page, size, result.getTotalElements(), result.getTotalPages());
    }

    private static String normalizeFilter(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        if (normalized.length() > 100) throw new CrudException(400, "INVALID_REQUEST");
        return normalized.toLowerCase(Locale.ROOT);
    }

    private static Specification<User> contains(String field, String value) {
        // Treat SQL LIKE wildcards as literal input, including the escape character itself.
        String pattern = "%" + value.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.<String>get(field)), pattern, '!');
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_USER_READ')")
    public Response get(UUID id) { return super.get(id); }

    @Override
    @PreAuthorize("hasAuthority('PERM_USER_WRITE')")
    public Response create(Request request) { return super.create(request); }

    @Override
    @PreAuthorize("hasAuthority('PERM_USER_WRITE')")
    public Response update(UUID id, Request request) {
        users.findLockedById(id).orElseThrow(() -> new CrudException(404, "NOT_FOUND"));
        return super.update(id, request);
    }

    @Override
    @PreAuthorize("hasAuthority('PERM_USER_WRITE')")
    public void delete(UUID id) { super.delete(id); }

    @Override
    protected User createEntity(Request request) {
        return new User(request.email().trim().toLowerCase(java.util.Locale.ROOT),
                encodePassword(request.password()),
                request.status(),
                request.emailVerifiedAt(),
                request.nome(),
                request.cognome());
    }

    @Override
    protected void updateEntity(User entity, Request request) {
        if (request.password() != null || !entity.getEmail().equalsIgnoreCase(request.email().trim()) ||
                !entity.getStatus().equals(request.status())) entity.invalidateCredentials();
        entity.setEmail(request.email().trim().toLowerCase(java.util.Locale.ROOT));
        if (request.password() != null) entity.setHashedPassword(encodePassword(request.password()));
        entity.setStatus(request.status());
        entity.setEmailVerifiedAt(request.emailVerifiedAt());
        entity.setNome(request.nome());
        entity.setCognome(request.cognome());
    }

    @Override
    protected Response response(User entity) {
        return new Response(entity.getId(), entity.getEmail(),
                entity.getStatus(),
                entity.getEmailVerifiedAt(),
                entity.getNome(),
                entity.getCognome());
    }

    private String encodePassword(String password) {
        if (password == null || password.isBlank() || password.length() < 12 ||
                password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new CrudException(400, "INVALID_PASSWORD");
        }
        return encoder.encode(password);
    }
}

