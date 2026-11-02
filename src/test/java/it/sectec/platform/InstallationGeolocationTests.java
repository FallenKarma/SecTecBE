package it.sectec.platform;

import it.sectec.platform.installation.service.GoogleGeocodingService;
import it.sectec.platform.installation.service.GoogleGeocodingService.Coordinates;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

@SpringBootTest(properties = "app.cors.allowed-origins=http://localhost:5173")
@AutoConfigureMockMvc @ActiveProfiles("test") @Testcontainers @Transactional
class InstallationGeolocationTests {
    @Container @ServiceConnection static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17.11");
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper json;
    @MockitoBean GoogleGeocodingService geocoding;

    private UUID type() {
        var id = UUID.randomUUID();
        jdbc.update("insert into tipo_impianto(id,nome) values (?, 'Allarme')", id);
        return id;
    }
    @Test
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    void blockedInstallationDeletionRestoresAssociations() throws Exception {
        var type = type(); var plant = UUID.randomUUID(); var client = UUID.randomUUID();
        var interventionType = UUID.randomUUID(); var intervention = UUID.randomUUID();
        try {
            jdbc.update("insert into impianto(id,id_tipo_impianto,nome,indirizzo,cap,comune) values (?,?,'Casa','Via Roma','00100','Roma')", plant, type);
            jdbc.update("insert into cliente(id,nome,cognome) values (?,'Mario','Rossi')", client);
            jdbc.update("insert into cliente_impianto(id,id_cliente,id_impianto) values (?,?,?)", UUID.randomUUID(), client, plant);
            jdbc.update("insert into tipo_intervento(id,nome) values (?,'Controllo')", interventionType);
            jdbc.update("insert into intervento(id,id_impianto,id_tipo_intervento,data) values (?,?,?,now())", intervention, plant, interventionType);
            mvc.perform(delete("/api/v1/impianti/" + plant).with(permissions("IMPIANTO_WRITE")).with(csrf())).andExpect(status().isConflict());
            assertThat(jdbc.queryForObject("select count(*) from cliente_impianto where id_impianto = ?", Long.class, plant)).isEqualTo(1L);
            assertThat(jdbc.queryForObject("select count(*) from impianto where id = ?", Long.class, plant)).isEqualTo(1L);
        } finally {
            jdbc.update("delete from intervento where id = ?", intervention);
            jdbc.update("delete from tipo_intervento where id = ?", interventionType);
            jdbc.update("delete from cliente_impianto where id_impianto = ?", plant);
            jdbc.update("delete from impianto where id = ?", plant);
            jdbc.update("delete from cliente where id = ?", client);
            jdbc.update("delete from tipo_impianto where id = ?", type);
        }
    }
    @Test void managesExistingAssociationsAndDeletesOnlyTheRequestedEntity() throws Exception {
        var type = type(); var plant = UUID.randomUUID();
        var first = UUID.randomUUID(); var second = UUID.randomUUID();
        jdbc.update("insert into impianto(id,id_tipo_impianto,nome,indirizzo,cap,comune) values (?,?,'Casa','Via Roma','00100','Roma')", plant, type);
        for (var id : List.of(first, second)) jdbc.update("insert into cliente(id,nome,cognome) values (?,'Mario','Rossi')", id);
        String path = "/api/v1/impianti/" + plant + "/clienti";
        String[] access = {"IMPIANTO_READ", "CLIENTE_READ", "CLIENTE_IMPIANTO_READ", "CLIENTE_IMPIANTO_WRITE"};
        mvc.perform(put(path).with(permissions(access)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("clientiIds", List.of(first, second, first)))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get(path).with(permissions(access))).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(put(path).with(permissions("IMPIANTO_READ", "CLIENTE_READ", "CLIENTE_IMPIANTO_READ")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"clientiIds\":[]}")).andExpect(status().isForbidden());
        mvc.perform(get(path).with(permissions("IMPIANTO_READ"))).andExpect(status().isForbidden());
        mvc.perform(put(path).with(permissions(access)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("clientiIds", List.of(UUID.randomUUID()))))).andExpect(status().isBadRequest());
        mvc.perform(get(path).with(permissions(access))).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(put(path).with(permissions(access)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("clientiIds", List.of(first)))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(delete("/api/v1/clienti/" + first).with(permissions("CLIENTE_READ")).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(delete("/api/v1/clienti/" + first).with(permissions("CLIENTE_WRITE")).with(csrf())).andExpect(status().isNoContent());
        assertThat(jdbc.queryForObject("select count(*) from impianto where id = ?", Long.class, plant)).isEqualTo(1L);
        mvc.perform(get(path).with(permissions(access))).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(put(path).with(permissions(access)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("clientiIds", List.of(second))))).andExpect(status().isOk());
        mvc.perform(delete("/api/v1/impianti/" + plant).with(permissions("IMPIANTO_READ")).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(delete("/api/v1/impianti/" + plant).with(permissions("IMPIANTO_WRITE")).with(csrf())).andExpect(status().isNoContent());
        assertThat(jdbc.queryForObject("select count(*) from cliente where id = ?", Long.class, second)).isEqualTo(1L);
        assertThat(jdbc.queryForObject("select count(*) from cliente_impianto where id_impianto = ?", Long.class, plant)).isZero();
    }
    @Test void creationAssociatesMultipleClientsAtomicallyAndChecksPermissions() throws Exception {
        var first = UUID.randomUUID();
        var second = UUID.randomUUID();
        for (var id : List.of(first, second)) jdbc.update("insert into cliente(id,nome,cognome) values (?,'Mario','Rossi')", id);
        var body = new HashMap<String, Object>(Map.of("tipoImpiantoId", type(), "nome", "Casa associata", "indirizzo", "Via Roma 1", "cap", "00100", "comune", "Roma", "clientiIds", List.of(first, second, first)));
        String[] access = {"IMPIANTO_WRITE", "CLIENTE_READ", "CLIENTE_IMPIANTO_WRITE"};
        var result = mvc.perform(post("/api/v1/impianti").with(permissions(access)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                .andExpect(status().isCreated()).andReturn();
        var id = UUID.fromString(json.readTree(result.getResponse().getContentAsString()).path("id").asText());
        assertThat(jdbc.queryForList("select id_cliente from cliente_impianto where id_impianto = ?", UUID.class, id)).containsExactlyInAnyOrder(first, second);
        for (String[] restricted : List.of(new String[]{"IMPIANTO_WRITE", "CLIENTE_READ"}, new String[]{"IMPIANTO_WRITE", "CLIENTE_IMPIANTO_WRITE"})) {
            mvc.perform(post("/api/v1/impianti").with(permissions(restricted)).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body))).andExpect(status().isForbidden());
        }
        var count = jdbc.queryForObject("select count(*) from impianto", Long.class);
        body.put("clientiIds", List.of(first, UUID.randomUUID()));
        mvc.perform(post("/api/v1/impianti").with(permissions(access)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body))).andExpect(status().isBadRequest());
        assertThat(jdbc.queryForObject("select count(*) from impianto", Long.class)).isEqualTo(count);
    }
    private org.springframework.test.web.servlet.request.RequestPostProcessor permissions(String... names) {
        return user("test").authorities(Arrays.stream(names).map(n -> new org.springframework.security.core.authority.SimpleGrantedAuthority("PERM_" + n)).toList());
    }
    @Test void createUpdateAndRecalculatePersistCoordinatesOrNullOnFailure() throws Exception {
        when(geocoding.locate(anyString(), anyString(), anyString())).thenReturn(Optional.of(new Coordinates(new BigDecimal("41.123456"), new BigDecimal("12.123456"))));
        var body = new HashMap<String, Object>(Map.of("tipoImpiantoId", type(), "nome", "Casa", "indirizzo", "Via Roma 1", "cap", "00100", "comune", "Roma"));
        var result = mvc.perform(post("/api/v1/impianti").with(permissions("IMPIANTO_WRITE")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.latitudine").value(41.123456)).andReturn();
        var id = json.readTree(result.getResponse().getContentAsString()).path("id").asText();
        clearInvocations(geocoding);
        body.put("nome", "Casa nuova"); body.put("latitudine", 10); body.put("longitudine", 20);
        mvc.perform(put("/api/v1/impianti/" + id).with(permissions("IMPIANTO_WRITE")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.latitudine").value(41.123456));
        verifyNoInteractions(geocoding);
        when(geocoding.locate(anyString(), anyString(), anyString())).thenReturn(Optional.empty());
        body.put("cap", "00101");
        mvc.perform(put("/api/v1/impianti/" + id).with(permissions("IMPIANTO_WRITE")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.latitudine").isEmpty()).andExpect(jsonPath("$.longitudine").isEmpty());
        verify(geocoding).locate("Via Roma 1", "00101", "Roma");
        mvc.perform(post("/api/v1/impianti").with(permissions("IMPIANTO_WRITE")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.latitudine").isEmpty());
        when(geocoding.locate(anyString(), anyString(), anyString())).thenReturn(Optional.of(new Coordinates(BigDecimal.ONE, BigDecimal.TEN)));
        mvc.perform(post("/api/v1/impianti/" + id + "/coordinate/ricalcola").with(permissions("IMPIANTO_WRITE")).with(csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.latitudine").value(1));
        assertThat(jdbc.queryForObject("select latitudine from impianto where id = ?", BigDecimal.class, UUID.fromString(id))).isEqualByComparingTo("1");
        mvc.perform(post("/api/v1/impianti/" + id + "/coordinate/ricalcola").with(permissions("IMPIANTO_READ")).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/impianti/" + id + "/coordinate/ricalcola").with(permissions("IMPIANTO_WRITE"))).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/impianti/" + UUID.randomUUID() + "/coordinate/ricalcola").with(permissions("IMPIANTO_WRITE")).with(csrf())).andExpect(status().isNotFound());
    }
    @Test void mapIncludesAllInstallationsAndOnlyAuthorizedClientNames() throws Exception {
        var type = type();
        var plant = UUID.randomUUID();
        for (int i = 0; i < 25; i++) jdbc.update("insert into impianto(id,id_tipo_impianto,nome,indirizzo,cap,comune,latitudine,longitudine) values (?,?,'Casa','Via Roma','00100','Roma',41,12)", i == 0 ? plant : UUID.randomUUID(), type);
        for (String name : List.of("Mario", "Anna")) {
            var client = UUID.randomUUID();
            jdbc.update("insert into cliente(id,nome,cognome) values (?,?,'Rossi')", client, name);
            jdbc.update("insert into cliente_impianto(id,id_cliente,id_impianto) values (?,?,?)", UUID.randomUUID(), client, plant);
        }
        var result = mvc.perform(get("/api/v1/impianti/mappa").with(permissions("IMPIANTO_READ", "CLIENTE_READ", "CLIENTE_IMPIANTO_READ")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(25)).andReturn();
        assertThat(result.getResponse().getContentAsString()).contains("Anna Rossi", "Mario Rossi");
        for (String[] access : List.of(new String[]{"IMPIANTO_READ"}, new String[]{"IMPIANTO_READ", "CLIENTE_READ"})) {
            result = mvc.perform(get("/api/v1/impianti/mappa").with(permissions(access))).andExpect(status().isOk()).andReturn();
            assertThat(result.getResponse().getContentAsString()).doesNotContain("Rossi");
        }
        mvc.perform(get("/api/v1/impianti/mappa").with(permissions("IMPIANTO_WRITE"))).andExpect(status().isForbidden());
    }
}
