package org.maia.autorizador.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.maia.autorizador.shared.persistence.FlywayLocalConfig;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Reproduz o banco local de quem subiu a API antes da V5: V1–V4 e a seed V1000 antiga já aplicadas. A subida com o
 * reparo do perfil local deve aplicar a V5 fora de ordem, converter as idades em faixas e aceitar o novo checksum.
 */
class FlywayLocalReparoTest {

    private static final String SEED_ANTIGA = """
            insert into procedimento (numero_procedimento) values (1234);
            insert into procedimento_sexo (procedimento_id, sexo) select id, 'F' from procedimento;
            insert into procedimento_idade (procedimento_id, idade) select id, i from procedimento, unnest(array[10, 20]) i;
            """;

    @Test
    void bancoLocalAntigoSobeComReparoEAplicaV5ForaDeOrdem(@TempDir Path antigo) throws Exception {
        for (String v : List.of("V1__schema_base", "V2__auditoria", "V3__usuario", "V4__catalogo_estendido")) {
            try (var in = getClass().getResourceAsStream("/db/migration/" + v + ".sql")) {
                Files.write(antigo.resolve(v + ".sql"), in.readAllBytes());
            }
        }
        Files.writeString(antigo.resolve("V1000__seed_local.sql"), SEED_ANTIGA);

        try (var postgres = new PostgreSQLContainer("postgres:16-alpine")) {
            postgres.start();
            var dataSource = new DriverManagerDataSource(postgres.getJdbcUrl(), postgres.getUsername(),
                    postgres.getPassword());
            Flyway.configure().dataSource(dataSource).locations("filesystem:" + antigo).load().migrate();

            Flyway atual = Flyway.configure().dataSource(dataSource)
                    .locations("classpath:db/migration", "classpath:db/seed").outOfOrder(true).load();
            FlywayLocalConfig.reparar(atual);

            var jdbc = new JdbcTemplate(dataSource);
            assertThat(jdbc.queryForList("select version from flyway_schema_history order by installed_rank",
                    String.class)).containsExactly("1", "2", "3", "4", "1000", "5");
            assertThat(jdbc.queryForList("""
                    select f.faixa from procedimento_faixa_etaria f order by f.faixa""", String.class))
                    .containsExactly("ADOLESCENTE", "CRIANCA");
        }
    }
}
