package org.maia.autorizador.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Perfil {@code local}: a seed entra pelas migrações. Usa contêiner próprio porque a seed (V1000) não pode
 * conviver, no mesmo banco, com contextos do perfil {@code test} (o Flyway rejeitaria a migração ausente).
 */
@SpringBootTest
@ActiveProfiles("local")
@TestPropertySource(properties = "app.jwt.secret=segredo-somente-para-testes-0123456789-abcdef")
class SeedLocalTest {

    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @Autowired
    JdbcTemplate jdbc;

    private Map<Integer, String> agrupar(String sql) {
        return jdbc.queryForList(sql).stream().collect(Collectors.toMap(
                l -> (Integer) l.get("numero"), l -> (String) l.get("valores")));
    }

    @Test
    void seedCriaTresProcedimentosComSexosEFaixasEtarias() {
        assertThat(jdbc.queryForList("select numero_procedimento from procedimento order by 1", Integer.class))
                .containsExactly(1234, 4567, 6789);

        assertThat(agrupar("""
                select p.numero_procedimento as numero, string_agg(s.sexo, ',' order by s.sexo) as valores
                from procedimento p join procedimento_sexo s on s.procedimento_id = p.id group by 1
                """)).containsEntry(1234, "F").containsEntry(4567, "F,M").containsEntry(6789, "F,M");

        assertThat(agrupar("""
                select p.numero_procedimento as numero, string_agg(f.faixa, ',' order by f.faixa) as valores
                from procedimento p join procedimento_faixa_etaria f on f.procedimento_id = p.id group by 1
                """)).containsEntry(1234, "ADOLESCENTE,CRIANCA").containsEntry(4567, "ADOLESCENTE,ADULTO")
                .containsEntry(6789, "CRIANCA");

        List<String> versoes = jdbc.queryForList(
                "select version from flyway_schema_history order by installed_rank", String.class);
        assertThat(versoes).containsExactly("1", "2", "3", "4", "5", "1000");
    }
}
