package it.sectec.platform.support.entity;

import it.sectec.platform.identity.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Entity
@Table(name = "user_intervento", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_intervento", columnNames = {"id_user", "id_intervento"})
})
public class UserIntervento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_intervento", nullable = false)
    private Intervento intervento;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    protected UserIntervento() {
    }

    public UserIntervento(Intervento intervento, User user) {
        this.intervento = intervento;
        this.user = user;
    }

    public UUID getId() {
        return id;
    }

    public Intervento getIntervento() {
        return intervento;
    }

    public void setIntervento(Intervento intervento) {
        this.intervento = intervento;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

