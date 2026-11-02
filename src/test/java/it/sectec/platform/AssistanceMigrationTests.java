package it.sectec.platform;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import static org.assertj.core.api.Assertions.*;

@Testcontainers
class AssistanceMigrationTests {
    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17.11");

    private Connection legacyDatabase(boolean linked) throws Exception {
        Connection connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        connection.setAutoCommit(false);
        try (var statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE impianto (id uuid PRIMARY KEY);
                CREATE TABLE articolo (id uuid PRIMARY KEY, id_impianto uuid REFERENCES impianto(id));
                CREATE TABLE assistenza (id uuid PRIMARY KEY, id_articolo uuid NOT NULL REFERENCES articolo(id), note text);
                CREATE INDEX ix_assistenza_id_articolo ON assistenza(id_articolo);
                INSERT INTO impianto VALUES ('00000000-0000-0000-0000-000000000001');
                INSERT INTO articolo VALUES ('00000000-0000-0000-0000-000000000002', NULL);
                INSERT INTO assistenza VALUES ('00000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000002', 'Preservare');
                """);
            if (linked) statement.execute("UPDATE articolo SET id_impianto = '00000000-0000-0000-0000-000000000001'");
        }
        return connection;
    }

    private String migration() throws Exception {
        return new ClassPathResource("db/migration/V9__move_assistance_to_installation.sql")
                .getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    void preservesExistingAssistanceAndReplacesArticleForeignKey() throws Exception {
        try (var connection = legacyDatabase(true); var statement = connection.createStatement()) {
            statement.execute(migration());
            try (var result = statement.executeQuery("SELECT id, id_impianto, note FROM assistenza")) {
                assertThat(result.next()).isTrue();
                assertThat(result.getString(1)).endsWith("0003");
                assertThat(result.getString(2)).endsWith("0001");
                assertThat(result.getString(3)).isEqualTo("Preservare");
            }
            // The covered article can now be removed without removing assistance.
            statement.execute("DELETE FROM articolo");
            try (var result = statement.executeQuery("SELECT count(*) FROM information_schema.columns WHERE table_name='assistenza' AND column_name='id_articolo'")) {
                result.next(); assertThat(result.getInt(1)).isZero();
            }
            assertThatThrownBy(() -> statement.execute("DELETE FROM impianto"))
                    .isInstanceOf(SQLException.class).hasMessageContaining("assistenza_id_impianto_fkey");
            connection.rollback();
        }
    }

    @Test
    void refusesToInventInstallationForUnassignedArticles() throws Exception {
        try (var connection = legacyDatabase(false); var statement = connection.createStatement()) {
            var savepoint = connection.setSavepoint();
            String sql = migration();
            assertThatThrownBy(() -> statement.execute(sql)).isInstanceOf(SQLException.class)
                    .hasMessageContaining("assign an installation");
            connection.rollback(savepoint);
            try (var result = statement.executeQuery("SELECT note FROM assistenza WHERE id_articolo IS NOT NULL")) {
                assertThat(result.next()).isTrue(); assertThat(result.getString(1)).isEqualTo("Preservare");
            }
            connection.rollback();
        }
    }
}
