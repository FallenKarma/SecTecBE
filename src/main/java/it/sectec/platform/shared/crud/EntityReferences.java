package it.sectec.platform.shared.crud;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.springframework.stereotype.Component;
@Component
public class EntityReferences {
    private final EntityManager em;
    public EntityReferences(EntityManager em) { this.em = em; }
    public <T> T find(Class<T> type, UUID id) {
        if (id == null) return null;
        T entity = em.find(type, id);
        if (entity == null) throw new CrudException(400, "INVALID_REFERENCE");
        return entity;
    }
}
