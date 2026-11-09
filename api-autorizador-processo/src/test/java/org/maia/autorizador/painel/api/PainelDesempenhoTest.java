package org.maia.autorizador.painel.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.maia.autorizador.support.IntegrationTestBase;
import org.maia.autorizador.support.JwtTestSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Referência para a SPEC-42: tempo e SQL de {@code GET /painel/resumo} com 1.000 procedimentos e 10.000 autorizações.
 * As 4 consultas agregadas usam {@code JdbcClient} (fixas); o Hibernate conta apenas as da listagem das últimas 4.
 */
@AutoConfigureMockMvc
class PainelDesempenhoTest extends IntegrationTestBase {

    private static final int CONSULTAS_AGREGADAS_JDBC = 4;

    @Autowired
    MockMvc mvc;

    @Autowired
    EntityManagerFactory emf;

    @Test
    void resumoComMuitosDadosTemSqlConstanteETempoRazoavel() throws Exception {
        jdbc.update("insert into procedimento (numero_procedimento) select g from generate_series(1, 1000) g");
        jdbc.update("insert into procedimento_sexo (procedimento_id, sexo) select id, 'M' from procedimento");
        jdbc.update("insert into procedimento_sexo (procedimento_id, sexo) select id, 'F' from procedimento "
                + "where id % 2 = 0");
        jdbc.update("insert into procedimento_faixa_etaria (procedimento_id, faixa) "
                + "select id, f from procedimento, unnest(array['CRIANCA','ADOLESCENTE','ADULTO','IDOSO']) f");
        jdbc.update("insert into autorizacao (procedimento_id, permitido, criada_em) "
                + "select 1 + (g % 1000), true, now() - (g * interval '1 second') from generate_series(1, 10000) g");

        Statistics estatisticas = emf.unwrap(SessionFactory.class).getStatistics();
        estatisticas.setStatisticsEnabled(true);
        try {
            chamar(); // aquecimento
            estatisticas.clear();
            long inicio = System.nanoTime();
            chamar();
            long ms = (System.nanoTime() - inicio) / 1_000_000;
            long sqlJpa = estatisticas.getPrepareStatementCount();
            System.out.println("[PAINEL-RESUMO-REFERENCIA] 1000 procedimentos / 10000 autorizacoes: tempo=" + ms
                    + " ms | SQL JPA=" + sqlJpa + " | SQL JDBC=" + CONSULTAS_AGREGADAS_JDBC + " | total="
                    + (sqlJpa + CONSULTAS_AGREGADAS_JDBC));
            assertThat(sqlJpa).as("SQL JPA constante (listagem + autenticação)").isLessThanOrEqualTo(5);
            assertThat(ms).as("tempo do resumo (ms)").isLessThan(2000);
        } finally {
            estatisticas.setStatisticsEnabled(false);
        }
    }

    private void chamar() throws Exception {
        mvc.perform(get("/api/v1/painel/resumo").header(HttpHeaders.AUTHORIZATION,
                JwtTestSupport.cabecalhoBearer(1L, "t@ex.com"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProcedimentos").value(1000))
                .andExpect(jsonPath("$.totalAutorizacoes").value(10000))
                .andExpect(jsonPath("$.faixasCobertas").value(4))
                .andExpect(jsonPath("$.distribuicaoSexos.masculino").value(1000))
                .andExpect(jsonPath("$.distribuicaoSexos.feminino").value(500));
    }
}
