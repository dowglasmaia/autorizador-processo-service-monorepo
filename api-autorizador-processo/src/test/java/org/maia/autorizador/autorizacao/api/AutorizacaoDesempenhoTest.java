package org.maia.autorizador.autorizacao.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.maia.autorizador.support.IntegrationTestBase;
import org.maia.autorizador.support.JwtTestSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.web.servlet.MockMvc;

/** Contagem de SQL de {@code GET /autorizacoes}: constante, independente do número de itens (sem N+1). */
@AutoConfigureMockMvc
class AutorizacaoDesempenhoTest extends IntegrationTestBase {

    private static final String BASE = "/api/v1/autorizacoes";

    @Autowired
    MockMvc mvc;

    @Autowired
    DataSource dataSource;

    @Autowired
    EntityManagerFactory emf;

    @BeforeEach
    void semear() {
        new ResourceDatabasePopulator(new ClassPathResource("db/seed/V1000__seed_local.sql")).execute(dataSource);
    }

    @Test
    void listagemUsaNumeroConstanteDeSqlIndependenteDoTamanhoDaPagina() throws Exception {
        Statistics estatisticas = emf.unwrap(SessionFactory.class).getStatistics();
        estatisticas.setStatisticsEnabled(true);
        try {
            criar(3);
            long comTres = sqlDaListagem(estatisticas, 3);
            criar(20);
            long comVinteETres = sqlDaListagem(estatisticas, 23);
            System.out.println("[SQL-LISTAGEM-AUTORIZACOES] 3 itens=" + comTres + " | 23 itens=" + comVinteETres);
            assertThat(comVinteETres).as("sem N+1: mesma contagem com mais itens").isEqualTo(comTres);
            assertThat(comTres).isLessThanOrEqualTo(4);
        } finally {
            estatisticas.setStatisticsEnabled(false);
        }
    }

    private void criar(int quantidade) throws Exception {
        for (int i = 0; i < quantidade; i++) {
            mvc.perform(post(BASE).header(HttpHeaders.AUTHORIZATION, JwtTestSupport.cabecalhoBearer(1L, "t@ex.com"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"numProcedimento\":4567,\"idade\":20,\"sexo\":\"F\"}"))
                    .andExpect(status().isCreated());
        }
    }

    private long sqlDaListagem(Statistics estatisticas, int esperados) throws Exception {
        estatisticas.clear();
        mvc.perform(get(BASE).header(HttpHeaders.AUTHORIZATION, JwtTestSupport.cabecalhoBearer(1L, "t@ex.com"))
                .param("size", "50")).andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItens").value(esperados));
        return estatisticas.getPrepareStatementCount();
    }
}
