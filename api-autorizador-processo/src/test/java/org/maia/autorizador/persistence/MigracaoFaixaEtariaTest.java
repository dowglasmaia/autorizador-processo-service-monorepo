package org.maia.autorizador.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** V5: as idades exatas já gravadas viram as faixas etárias que as contêm (limites 12/13, 20/21, 59/60). */
class MigracaoFaixaEtariaTest {

    @Test
    void idadesViramFaixasPeloLimiteDeCada() {
        try (var postgres = new PostgreSQLContainer("postgres:16-alpine")) {
            postgres.start();
            var dataSource = new DriverManagerDataSource(postgres.getJdbcUrl(), postgres.getUsername(),
                    postgres.getPassword());
            var jdbc = new JdbcTemplate(dataSource);

            Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").target("4").load().migrate();
            jdbc.update("insert into procedimento (numero_procedimento) values (1), (2)");
            jdbc.update("""
                    insert into procedimento_idade (procedimento_id, idade)
                    select p.id, i.idade from procedimento p
                    join (values (1, 0), (1, 12), (1, 13), (1, 20), (2, 21), (2, 59), (2, 60), (2, 150))
                        as i (numero, idade) on i.numero = p.numero_procedimento
                    """);

            Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();

            List<String> faixas1 = faixasDe(jdbc, 1);
            List<String> faixas2 = faixasDe(jdbc, 2);
            assertThat(faixas1).containsExactly("ADOLESCENTE", "CRIANCA");
            assertThat(faixas2).containsExactly("ADULTO", "IDOSO");
            assertThat(jdbc.queryForObject(
                    "select count(*) from information_schema.tables where table_name = 'procedimento_idade'",
                    Integer.class)).isZero();
        }
    }

    private static List<String> faixasDe(JdbcTemplate jdbc, int numero) {
        return jdbc.queryForList("""
                select f.faixa from procedimento_faixa_etaria f join procedimento p on p.id = f.procedimento_id
                where p.numero_procedimento = ? order by f.faixa
                """, String.class, numero);
    }
}
