package it.sectec.platform.installation.entity;

import it.sectec.platform.customer.entity.Cliente;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Entity
@Table(name = "cliente_impianto", uniqueConstraints = {
        @UniqueConstraint(name = "uq_cliente_impianto", columnNames = {"id_cliente", "id_impianto"})
})
public class ClienteImpianto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_impianto", nullable = false)
    private Impianto impianto;

    protected ClienteImpianto() {
    }

    public ClienteImpianto(Cliente cliente, Impianto impianto) {
        this.cliente = cliente;
        this.impianto = impianto;
    }

    public UUID getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Impianto getImpianto() {
        return impianto;
    }

    public void setImpianto(Impianto impianto) {
        this.impianto = impianto;
    }
}

