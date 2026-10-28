package it.sectec.platform;

import com.jayway.jsonpath.JsonPath;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
class CrudApiTests {
    @Container @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17.11");
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder encoder;

    private MockHttpServletRequestBuilder authorized(MockHttpServletRequestBuilder request) {
        var permissions = jdbc.queryForList("select name from permission", String.class).stream()
                .map(name -> new SimpleGrantedAuthority("PERM_" + name)).toList();
        var adminAuthorities = new ArrayList<>(permissions);
        adminAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        return request.with(user("crud-test").authorities(adminAuthorities)).with(csrf());
    }

    record Fixture(String entity, String path, Map<String, Object> body) {}

    @Test
    void changingUserRolesRequiresAdminAndWritePermission() throws Exception {
        String path = "/api/v1/user-roles";
        String body = json.writeValueAsString(Map.of("userId", UUID.randomUUID(),
                "roleId", UUID.randomUUID(), "assignedAt", "2026-10-28T12:00:00Z"));
        for (String authority : List.of("PERM_USER_ROLE_WRITE", "ROLE_ADMIN")) {
            for (var request : List.of(post(path).contentType(MediaType.APPLICATION_JSON).content(body),
                    put(path + "/" + UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content(body),
                    delete(path + "/" + UUID.randomUUID()))) {
                mvc.perform(request.with(user("restricted").authorities(new SimpleGrantedAuthority(authority))).with(csrf()))
                        .andExpect(status().isForbidden());
            }
        }
        // With both authorities the request reaches validation/persistence.
        mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body).with(csrf())
                .with(user("admin").authorities(new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("PERM_USER_ROLE_WRITE"))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REFERENCE"));
    }

    @Test
    void clientInstallationsReturnAllLinkedInstallationsIncludingSharedOnes() throws Exception {
        UUID first = cliente("Mario", "Rossi", null);
        UUID other = cliente("Anna", "Verdi", null);
        UUID empty = cliente("Luca", "Bianchi", null);
        UUID tipo = UUID.randomUUID();
        jdbc.update("insert into tipo_impianto(id,nome) values (?, 'Test')", tipo);
        var expected = new java.util.ArrayList<String>();
        // More than the default page size: this endpoint must return the complete list.
        for (int n = 0; n < 25; n++) {
            UUID id = UUID.randomUUID();
            jdbc.update("""
                    insert into impianto(id,id_tipo_impianto,indirizzo,cap,nome,comune)
                    values (?,?,'Via Test','00100',?,'Roma')
                    """, id, tipo, "Impianto " + n);
            jdbc.update("insert into cliente_impianto(id,id_cliente,id_impianto) values (?,?,?)",
                    UUID.randomUUID(), n < 24 ? first : other, id);
            if (n < 24) expected.add(id.toString());
            if (n == 0) jdbc.update("insert into cliente_impianto(id,id_cliente,id_impianto) values (?,?,?)",
                    UUID.randomUUID(), other, id);
        }
        expected.sort(String::compareTo);
        var response = mvc.perform(authorized(get("/api/v1/clienti/" + first + "/impianti")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(24))
                .andExpect(jsonPath("$[0].tipoImpiantoId").value(tipo.toString()))
                .andExpect(jsonPath("$[0].comune").value("Roma"))
                .andExpect(jsonPath("$[0].cliente").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        java.util.List<String> ids = JsonPath.read(response, "$[*].id");
        assertThat(ids).containsExactlyElementsOf(expected);
        mvc.perform(authorized(get("/api/v1/clienti/" + other + "/impianti")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(authorized(get("/api/v1/clienti/" + empty + "/impianti")))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void clientInstallationsRequireBothReadPermissionsAndValidateClientId() throws Exception {
        UUID id = cliente("Mario", "Rossi", null);
        String path = "/api/v1/clienti/" + id + "/impianti";
        mvc.perform(get(path)).andExpect(status().isUnauthorized());
        for (String permission : List.of("PERM_CLIENTE_READ", "PERM_IMPIANTO_READ", "PERM_CLIENTE_WRITE")) {
            mvc.perform(get(path).with(user("reader").authorities(new SimpleGrantedAuthority(permission))))
                    .andExpect(status().isForbidden());
        }
        mvc.perform(get(path).with(user("reader").authorities(
                new SimpleGrantedAuthority("PERM_CLIENTE_READ"), new SimpleGrantedAuthority("PERM_IMPIANTO_READ"))))
                .andExpect(status().isOk());
        mvc.perform(authorized(get("/api/v1/clienti/" + UUID.randomUUID() + "/impianti")))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("NOT_FOUND"));
        mvc.perform(authorized(get("/api/v1/clienti/invalid/impianti")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    private UUID cliente(String nome, String cognome, String codiceFiscale) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into cliente(id,nome,cognome,codice_fiscale) values (?,?,?,?)",
                id, nome, cognome, codiceFiscale);
        return id;
    }

    private UUID searchUser(String nome, String cognome) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                insert into app_user(id,email,hashed_password,status,nome,cognome)
                values (?,?,'unused-test-hash','ACTIVE',?,?)
                """, id, id + "@example.com", nome, cognome);
        return id;
    }

    @Test
    void usersCanBeSearchedByCombinedPartialNames() throws Exception {
        UUID mario = searchUser("Mario", "Rossi");
        searchUser("Maria", "Verdi");
        searchUser("Luca", "Rossi");
        mvc.perform(authorized(get("/api/v1/users/search").param("nome", " ARI ")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2));
        mvc.perform(authorized(get("/api/v1/users/search").param("cognome", " OSS ")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2));
        mvc.perform(authorized(get("/api/v1/users/search").param("nome", " MAR ").param("cognome", " ROS ")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(mario.toString()))
                .andExpect(jsonPath("$.content[0].hashedPassword").doesNotExist());
        mvc.perform(authorized(get("/api/v1/users/search").param("nome", "luca").param("cognome", "verdi")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void userSearchHandlesLiteralWildcardsBlankFiltersAndPagination() throws Exception {
        UUID literal = searchUser("Special%_!", "Test%_!");
        searchUser("SpecialABC", "TestABC");
        searchUser("Other", "Test");
        for (String field : List.of("nome", "cognome")) {
            mvc.perform(authorized(get("/api/v1/users/search").param(field, "%_!")))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.content[0].id").value(literal.toString()));
        }
        var unfiltered = mvc.perform(authorized(get("/api/v1/users").param("size", "2").param("page", "1")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        mvc.perform(authorized(get("/api/v1/users/search").param("nome", " ").param("cognome", "")
                .param("size", "2").param("page", "1")))
                .andExpect(status().isOk()).andExpect(content().json(unfiltered));
        mvc.perform(authorized(get("/api/v1/users/search").param("nome", "Special").param("size", "1")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2)).andExpect(jsonPath("$.totalPages").value(2));
        mvc.perform(authorized(get("/api/v1/users/search").param("page", "99")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void userSearchRequiresReadPermissionAndValidFiltersAndPagination() throws Exception {
        mvc.perform(get("/api/v1/users/search")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/users/search").with(user("writer")
                .authorities(new SimpleGrantedAuthority("PERM_USER_WRITE"))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/users/search").with(user("reader")
                .authorities(new SimpleGrantedAuthority("PERM_USER_READ"))))
                .andExpect(status().isOk());
        for (String field : List.of("nome", "cognome")) {
            mvc.perform(authorized(get("/api/v1/users/search").param(field, "x".repeat(101))))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        }
        for (String size : List.of("0", "101")) {
            mvc.perform(authorized(get("/api/v1/users/search").param("size", size)))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_PAGINATION"));
        }
        mvc.perform(authorized(get("/api/v1/users/search").param("page", "-1")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_PAGINATION"));
    }

    @Test
    void clientsCanBeSearchedByCombinedNamesAndExactTaxCode() throws Exception {
        UUID mario = cliente("Mario", "Rossi", "RSSMRA80A01H501U");
        cliente("Maria", "Verdi", "VRDMRA80A01H501U");
        cliente("Luca", "Rossi", null);
        mvc.perform(authorized(get("/api/v1/clienti/search").param("nome", " ARI ")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2));
        mvc.perform(authorized(get("/api/v1/clienti/search").param("cognome", "oss")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2));
        mvc.perform(authorized(get("/api/v1/clienti/search").param("nome", " MAR ").param("cognome", " ROS ")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(mario.toString()));
        mvc.perform(authorized(get("/api/v1/clienti/search").param("codiceFiscale", " rssmra80a01h501u ")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(mario.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(authorized(get("/api/v1/clienti/search").param("nome", "mario")
                .param("cognome", "rossi").param("codiceFiscale", "RSSMRA80A01H501U")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(authorized(get("/api/v1/clienti/search").param("nome", "luca")
                .param("codiceFiscale", "RSSMRA80A01H501U")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(authorized(get("/api/v1/clienti/search").param("codiceFiscale", "RSS")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void clientSearchHandlesLiteralWildcardsBlankFiltersAndStablePagination() throws Exception {
        UUID literal = cliente("Special%_!", "Test", null);
        cliente("SpecialABC", "Test", null);
        cliente("Other", "Test", null);
        mvc.perform(authorized(get("/api/v1/clienti/search").param("nome", "%_!")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(literal.toString()));
        var unfiltered = mvc.perform(authorized(get("/api/v1/clienti").param("size", "2").param("page", "1")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        mvc.perform(authorized(get("/api/v1/clienti/search").param("nome", " ").param("cognome", "")
                .param("codiceFiscale", " ").param("size", "2").param("page", "1")))
                .andExpect(status().isOk()).andExpect(content().json(unfiltered));
        mvc.perform(authorized(get("/api/v1/clienti/search").param("nome", "Special").param("size", "1")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2)).andExpect(jsonPath("$.totalPages").value(2));
        mvc.perform(authorized(get("/api/v1/clienti/search").param("page", "99")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void clientSearchRequiresReadPermissionAndValidFiltersAndPagination() throws Exception {
        mvc.perform(get("/api/v1/clienti/search").param("nome", "Mario"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/clienti/search").with(user("writer")
                .authorities(new SimpleGrantedAuthority("PERM_CLIENTE_WRITE"))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/clienti/search").with(user("reader")
                .authorities(new SimpleGrantedAuthority("PERM_CLIENTE_READ"))))
                .andExpect(status().isOk());
        for (String field : List.of("nome", "cognome", "codiceFiscale")) {
            mvc.perform(authorized(get("/api/v1/clienti/search").param(field, "x".repeat(101))))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        }
        for (String size : List.of("0", "101")) {
            mvc.perform(authorized(get("/api/v1/clienti/search").param("size", size)))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_PAGINATION"));
        }
        mvc.perform(authorized(get("/api/v1/clienti/search").param("page", "-1")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_PAGINATION"));
        mvc.perform(authorized(get("/api/v1/clienti/search").param("page", "abc")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void allTwentyThreeResourcesSupportCrudWithIdsInsteadOfNestedEntities() throws Exception {
        Fixture[] fixtures;
        try (var stream = new ClassPathResource("crud-fixtures.json").getInputStream()) {
            fixtures = json.readValue(stream, Fixture[].class);
        }
        Map<String, String> ids = new HashMap<>();
        for (Fixture fixture : fixtures) {
            var body = new LinkedHashMap<>(fixture.body());
            body.replaceAll((key, value) -> value instanceof String text && text.startsWith("$")
                    ? UUID.fromString(Objects.requireNonNull(ids.get(text.substring(1)))) : value);
            String payload = json.writeValueAsString(body);
            var created = mvc.perform(authorized(post("/api/v1/" + fixture.path()))
                    .contentType(MediaType.APPLICATION_JSON).content(payload))
                    .andExpect(status().isCreated()).andExpect(header().exists("Location")).andReturn();
            String response = created.getResponse().getContentAsString();
            String id = JsonPath.read(response, "$.id");
            ids.put(fixture.entity(), id);
            String location = "/api/v1/" + fixture.path() + "/" + id;
            mvc.perform(authorized(get(location))).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
            mvc.perform(authorized(get("/api/v1/" + fixture.path()).param("size", "100")))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").isNumber());
            if (fixture.entity().equals("User")) {
                assertThat(response).doesNotContain("password", "hashedPassword", "hashed_password");
                String hash = jdbc.queryForObject("select hashed_password from app_user where id = ?", String.class, UUID.fromString(id));
                assertThat(hash).isNotEqualTo(body.get("password"));
                assertThat(encoder.matches((String) body.get("password"), hash)).isTrue();
                body.put("password", null);
            } else if (body.containsKey("nome")) {
                body.put("nome", "Nome aggiornato");
            }
            mvc.perform(authorized(put(location)).contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(body))).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
            if (fixture.entity().equals("User")) {
                String hash = jdbc.queryForObject("select hashed_password from app_user where id = ?", String.class, UUID.fromString(id));
                assertThat(encoder.matches("Crud-password-123", hash)).isTrue();
            }
            if (body.containsKey("nome") && !fixture.entity().equals("User")) {
                mvc.perform(authorized(get(location))).andExpect(jsonPath("$.nome").value("Nome aggiornato"));
            }
        }
        assertThat(ids).hasSize(23);
        for (int i = fixtures.length - 1; i >= 0; i--) {
            String location = "/api/v1/" + fixtures[i].path() + "/" + ids.get(fixtures[i].entity());
            mvc.perform(authorized(delete(location))).andExpect(status().isNoContent());
            mvc.perform(authorized(get(location))).andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("NOT_FOUND"));
        }
    }

    @Test
    void permissionsAndCsrfAreRequired() throws Exception {
        mvc.perform(get("/api/v1/clienti")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/clienti").with(user("no-permissions"))).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/categorie").with(user("reader").authorities(new SimpleGrantedAuthority("PERM_CATEGORIA_READ")))
                .with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Test\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/categorie").with(user("writer").authorities(new SimpleGrantedAuthority("PERM_CATEGORIA_WRITE")))
                .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Test\"}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("INVALID_CSRF_TOKEN"));
        mvc.perform(get("/api/v1/categorie").with(user("reader").authorities(new SimpleGrantedAuthority("PERM_CATEGORIA_READ"))))
                .andExpect(status().isOk());
    }

    @Test
    void invalidInputPaginationAndReferencesReturnSafeErrors() throws Exception {
        mvc.perform(authorized(post("/api/v1/categorie")).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\" \"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        mvc.perform(authorized(get("/api/v1/categorie").param("size", "101")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_PAGINATION"));
        mvc.perform(authorized(get("/api/v1/categorie/not-a-uuid"))).andExpect(status().isBadRequest());
        String missing = json.writeValueAsString(Map.of("clienteId", UUID.randomUUID(), "impiantoId", UUID.randomUUID()));
        mvc.perform(authorized(post("/api/v1/clienti-impianti")).contentType(MediaType.APPLICATION_JSON).content(missing))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REFERENCE"));
    }

    @Test
    void duplicateCategoryReturnsConflict() throws Exception {
        var payload = "{\"nome\":\"Duplicata\"}";
        mvc.perform(authorized(post("/api/v1/categorie")).contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated());
        mvc.perform(authorized(post("/api/v1/categorie")).contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isConflict()).andExpect(content().json("{\"code\":\"DATA_CONFLICT\"}"));
    }

    @Test
    void deletingReferencedCategoryReturnsConflict() throws Exception {
        UUID category = UUID.randomUUID();
        jdbc.update("insert into categoria(id,nome) values (?, 'Usata')", category);
        jdbc.update("insert into tipo_articolo(id,id_categoria,codice_articolo,descrizione,marca,modello) values (?,?,'CODE','Desc','Marca','Modello')",
                UUID.randomUUID(), category);
        mvc.perform(authorized(delete("/api/v1/categorie/" + category)))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("DATA_CONFLICT"));
    }
}

