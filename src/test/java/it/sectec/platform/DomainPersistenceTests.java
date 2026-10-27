package it.sectec.platform;

import it.sectec.platform.catalog.entity.*;
import it.sectec.platform.customer.entity.Cliente;
import it.sectec.platform.identity.entity.User;
import it.sectec.platform.installation.entity.*;
import it.sectec.platform.inventory.entity.*;
import it.sectec.platform.storage.entity.FileAsset;
import it.sectec.platform.support.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
class DomainPersistenceTests {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17.11");

    @Autowired EntityManager em;
    @Autowired JdbcTemplate jdbc;

    private <T> T save(T entity) {
        em.persist(entity);
        return entity;
    }

    @Test
    void allDiagramTablesAreMapped() {
        assertThat(em.getMetamodel().getEntities().stream()
                .map(e -> e.getJavaType().getAnnotation(Table.class).name()))
                .containsExactlyInAnyOrder("app_user", "role", "permission", "user_role", "role_permission",
                        "cliente", "cliente_impianto", "impianto", "tipo_impianto", "categoria", "tipo_articolo",
                        "articolo", "assistenza", "tipo_assistenza", "intervento", "tipo_intervento", "user_intervento",
                        "file_asset", "immagine_articolo", "documento_articolo", "fornitore", "bolla", "bolla_tipo_articolo");
    }

    @Test
    void completeGraphRoundTripsWithAssociationsAndPreciseValues() {
        var user = save(new User("operator@example.test", "test-hash", "ACTIVE", null, "Mario", "Rossi"));
        var cliente = save(new Cliente(user, null, null, null, null, null, null, "Mario", "Rossi", null, null));
        var otherCliente = save(new Cliente(null, null, null, null, null, null, null, "Anna", "Verdi", null, null));
        var tipoImpianto = save(new TipoImpianto("Allarme"));
        var impianto = save(new Impianto(tipoImpianto, "Via Roma 1", "00100", "Casa", null, null, null,
                new BigDecimal("12.123456"), new BigDecimal("41.123456"), "Roma"));
        var clienteImpianto = save(new ClienteImpianto(cliente, impianto));
        save(new ClienteImpianto(otherCliente, impianto));
        var categoria = save(new Categoria("Sensori", null));
        var tipoArticolo = save(new TipoArticolo(categoria, "SENSOR-1", "Sensore", "Marca", "Modello", new BigDecimal("19.95")));
        var articolo = save(new Articolo(tipoArticolo, impianto, "SERIAL-1", "INSTALLATO"));
        var stock = save(new Articolo(tipoArticolo, null, null, "MAGAZZINO"));
        var tipoAssistenza = save(new TipoAssistenza("Annuale"));
        var assistenza = save(new Assistenza(articolo, tipoAssistenza, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1), null));
        var tipoIntervento = save(new TipoIntervento("Manutenzione"));
        var intervento = save(new Intervento(impianto, tipoIntervento, assistenza, Instant.parse("2026-10-25T10:00:00Z"), null, null));
        var fuoriCopertura = save(new Intervento(impianto, tipoIntervento, null, Instant.now(), null, null));
        var assegnazione = save(new UserIntervento(intervento, user));
        var secondoOperatore = save(new User("second@example.test", "test-hash", "ACTIVE", null, "Luca", "Bianchi"));
        save(new UserIntervento(intervento, secondoOperatore));
        var asset = save(new FileAsset("LOCAL", "assets", "manuals/manual.pdf", "manual.pdf", "application/pdf",
                100L, "a".repeat(64), "PRIVATE", Instant.parse("2026-10-25T09:00:00Z")));
        var immagine = save(new ImmagineArticolo(asset, "Immagine illustrativa", 0, tipoArticolo));
        var documento = save(new DocumentoArticolo(asset, "1", tipoArticolo));
        var fornitore = save(new Fornitore("Fornitore Srl", "Via Milano 1", null, null, null, null, null));
        var bolla = save(new Bolla(fornitore, "DDT-1", LocalDate.of(2026, 10, 25), null, "ARRIVO"));
        var bollaSenzaFornitore = save(new Bolla(null, "DDT-2", LocalDate.of(2026, 10, 25), null, "USCITA"));
        var riga = save(new BollaTipoArticolo(bolla, tipoArticolo, 3));
        em.flush();
        em.clear();

        assertThat(em.find(ClienteImpianto.class, clienteImpianto.getId()).getCliente().getUser().getId()).isEqualTo(user.getId());
        assertThat(em.find(Impianto.class, impianto.getId()).getTipoImpianto().getNome()).isEqualTo("Allarme");
        assertThat(em.find(Impianto.class, impianto.getId()).getLongitudine()).isEqualByComparingTo("12.123456");
        assertThat(em.find(UserIntervento.class, assegnazione.getId()).getIntervento().getTipoIntervento().getNome()).isEqualTo("Manutenzione");
        assertThat(em.find(Intervento.class, intervento.getId()).getAssistenza().getTipoAssistenza().getNome()).isEqualTo("Annuale");
        assertThat(em.find(Assistenza.class, assistenza.getId()).getArticolo().getImpianto().getId()).isEqualTo(impianto.getId());
        assertThat(em.find(Articolo.class, stock.getId()).getImpianto()).isNull();
        assertThat(em.find(Intervento.class, fuoriCopertura.getId()).getAssistenza()).isNull();
        assertThat(em.find(DocumentoArticolo.class, documento.getId()).getFileAsset().getSha256()).isEqualTo("a".repeat(64));
        assertThat(em.find(ImmagineArticolo.class, immagine.getId()).getTipoArticolo().getCategoria().getNome()).isEqualTo("Sensori");
        assertThat(em.find(BollaTipoArticolo.class, riga.getId()).getBolla().getFornitore().getId()).isEqualTo(fornitore.getId());
        assertThat(em.find(BollaTipoArticolo.class, riga.getId()).getTipoArticolo().getCosto()).isEqualByComparingTo("19.95");
        assertThat(em.find(Bolla.class, bollaSenzaFornitore.getId()).getFornitore()).isNull();
        assertThat(em.createQuery("select count(c) from ClienteImpianto c", Long.class).getSingleResult()).isEqualTo(2);
        assertThat(em.createQuery("select count(u) from UserIntervento u", Long.class).getSingleResult()).isEqualTo(2);
    }

    @Test
    void databaseRejectsDuplicateClientInstallationLink() {
        var cliente = save(new Cliente(null, null, null, null, null, null, null, "Mario", "Rossi", null, null));
        var tipo = save(new TipoImpianto("Allarme"));
        var impianto = save(new Impianto(tipo, "Via Roma 1", "00100", "Casa", null, null, null, null, null, "Roma"));
        save(new ClienteImpianto(cliente, impianto));
        em.flush();
        assertThatThrownBy(() -> jdbc.update("insert into cliente_impianto (id,id_cliente,id_impianto) values (?,?,?)",
                UUID.randomUUID(), cliente.getId(), impianto.getId())).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void databaseRejectsInvalidDeliveryDirection() {
        assertThatThrownBy(() -> jdbc.update("insert into bolla (id,numero_bolla,data_bolla,direzione) values (?, 'X', current_date, 'INVALID')",
                UUID.randomUUID())).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void databasePreventsDeletingReferencedCategory() {
        var categoria = save(new Categoria("Sensori", null));
        save(new TipoArticolo(categoria, "S1", "Sensore", "Marca", "Modello", null));
        em.flush();
        assertThatThrownBy(() -> jdbc.update("delete from categoria where id = ?", categoria.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
