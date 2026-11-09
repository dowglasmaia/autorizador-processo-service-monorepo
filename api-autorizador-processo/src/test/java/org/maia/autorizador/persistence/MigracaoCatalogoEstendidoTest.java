package org.maia.autorizador.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Migração V4 (catálogo estendido) sobre um banco com dados de V1–V3, em PostgreSQL 16 real: nenhuma linha se perde,
 * as linhas anteriores ficam com os campos novos nulos e os CHECKs rejeitam valores inválidos.
 */
class MigracaoCatalogoEstendidoTest {

    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");
    private static JdbcTemplate jdbc;

    @BeforeAll
    static void migrarComDadosAntesEDepoisDaV4() {
        POSTGRES.start();
        var dataSource = new DriverManagerDataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(),
                POSTGRES.getPassword());
        jdbc = new JdbcTemplate(dataSource);

        Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").target("3").load().migrate();
        jdbc.update("insert into procedimento (numero_procedimento) values (1234), (4567), (6789)");
        jdbc.update("insert into procedimento_sexo (procedimento_id, sexo) select id, 'F' from procedimento");
        jdbc.update("insert into procedimento_idade (procedimento_id, idade) select id, 10 from procedimento");

        Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();
    }

    @AfterAll
    static void encerrar() {
        POSTGRES.stop();
    }

    private static int contar(String tabela) {
        return jdbc.queryForObject("select count(*) from " + tabela, Integer.class);
    }

    @Test
    void v4NaoPerdeLinhasENaoPreencheOsCamposNovos() {
        assertThat(contar("procedimento")).isEqualTo(3);
        assertThat(contar("procedimento_sexo")).isEqualTo(3);
        assertThat(contar("procedimento_faixa_etaria")).isEqualTo(3);
        assertThat(jdbc.queryForObject("""
                select count(*) from procedimento where nome is null and codigo_tuss is null and categoria is null
                  and especialidade is null and tipo_cobertura is null and quantidade_maxima is null
                  and periodo is null and exige_autorizacao_previa is null and exige_justificativa_medica is null
                """, Integer.class)).isEqualTo(3);
        assertThat(jdbc.queryForObject("select max(version::int) from flyway_schema_history", Integer.class))
                .isEqualTo(5);
    }

    @Test
    void auditoriaGanhouAsColunasNovas() {
        assertThat(jdbc.queryForList("""
                select column_name from information_schema.columns
                where table_name = 'procedimento_audit' and column_name in ('nome', 'codigo_tuss', 'categoria',
                  'especialidade', 'tipo_cobertura', 'quantidade_maxima', 'periodo', 'exige_autorizacao_previa',
                  'exige_justificativa_medica')
                """, String.class)).hasSize(9);
        assertThat(jdbc.queryForList("""
                select column_name from information_schema.columns
                where table_name = 'procedimento_sexo_audit' and column_name = 'nome'
                """, String.class)).isEmpty();
    }

    @Test
    void aceitaValoresValidos() {
        jdbc.update("""
                insert into procedimento (numero_procedimento, nome, categoria, especialidade, tipo_cobertura,
                  quantidade_maxima, periodo) values (9001, 'Eco', 'EXAMES_IMAGEM', 'CARDIOLOGIA', 'REGRAS', 2, 'ANO')
                """);
        jdbc.update("delete from procedimento where numero_procedimento = 9001");
    }

    @Test
    void checksRejeitamValoresInvalidos() {
        assertRejeita("categoria", "'INEXISTENTE'", 9101);
        assertRejeita("especialidade", "'INEXISTENTE'", 9102);
        assertRejeita("tipo_cobertura", "'INEXISTENTE'", 9103);
        assertRejeita("periodo", "'SEMANA'", 9104);
        assertRejeita("quantidade_maxima, periodo", "0, 'MES'", 9105);
        assertRejeita("quantidade_maxima", "2", 9106);
    }

    private static void assertRejeita(String colunas, String valores, int numero) {
        assertThatThrownBy(() -> jdbc.update("insert into procedimento (numero_procedimento, " + colunas
                + ") values (" + numero + ", " + valores + ")")).isInstanceOf(DataIntegrityViolationException.class);
    }
}
