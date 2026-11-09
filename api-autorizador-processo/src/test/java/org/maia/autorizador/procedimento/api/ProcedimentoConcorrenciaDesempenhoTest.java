package org.maia.autorizador.procedimento.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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

/** Concorrência no cadastro e contagem de SQL da listagem (sem N+1). */
@AutoConfigureMockMvc
class ProcedimentoConcorrenciaDesempenhoTest extends IntegrationTestBase {

    private static final String BASE = "/api/v1/procedimentos";

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
    void duasInsercoesSimultaneasDoMesmoNumeroGeramUm201EUm403() throws Exception {
        String token = JwtTestSupport.cabecalhoBearer(1L, "teste@ex.com");
        String json = "{\"nome\":\"Proc\",\"categoria\":\"CONSULTAS\",\"numProcedimento\":7777,\"sexos\":[\"M\"],\"faixasEtarias\":[\"CRIANCA\"]}";
        int tentativas = 2;
        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(tentativas);
        try {
            List<Future<Integer>> futuros = new ArrayList<>();
            for (int i = 0; i < tentativas; i++) {
                futuros.add(pool.submit(() -> {
                    largada.await();
                    return mvc.perform(post(BASE).header(HttpHeaders.AUTHORIZATION, token)
                            .contentType(MediaType.APPLICATION_JSON).content(json))
                            .andReturn().getResponse().getStatus();
                }));
            }
            largada.countDown();
            List<Integer> codigos = new ArrayList<>();
            for (Future<Integer> f : futuros) {
                codigos.add(f.get(30, TimeUnit.SECONDS));
            }
            assertThat(codigos).containsExactlyInAnyOrder(201, 403);
        } finally {
            pool.shutdownNow();
        }
        assertThat(jdbc.queryForObject("select count(*) from procedimento where numero_procedimento = 7777",
                Integer.class)).isEqualTo(1);
    }

    @Test
    void listagemUsaNumeroConstanteDeSqlIndependenteDoTamanhoDaPagina() throws Exception {
        Statistics estatisticas = emf.unwrap(SessionFactory.class).getStatistics();
        estatisticas.setStatisticsEnabled(true);
        try {
            long comTres = sqlDaListagem(estatisticas);
            for (int n = 100; n < 120; n++) {
                mvc.perform(post(BASE).header(HttpHeaders.AUTHORIZATION, JwtTestSupport.cabecalhoBearer(1L, "t@ex.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Proc\",\"categoria\":\"CONSULTAS\",\"numProcedimento\":" + n + ",\"sexos\":[\"M\",\"F\"],\"idades\":[1,2,3]}"))
                        .andExpect(status().isCreated());
            }
            long comVinteETres = sqlDaListagem(estatisticas);
            System.out.println("[SQL-LISTAGEM] 3 itens=" + comTres + " | 23 itens=" + comVinteETres);
            assertThat(comVinteETres).as("sem N+1: mesma contagem com mais itens").isEqualTo(comTres);
            assertThat(comTres).isLessThanOrEqualTo(4);
        } finally {
            estatisticas.setStatisticsEnabled(false);
        }
    }

    /** Regressão da etapa 30: com @BatchSize(200) a contagem não cresce nem na página máxima (200 itens). */
    @Test
    void listagemNaPaginaMaximaMantemAMesmaContagemDeSql() throws Exception {
        Statistics estatisticas = emf.unwrap(SessionFactory.class).getStatistics();
        estatisticas.setStatisticsEnabled(true);
        try {
            var jdbc = new org.springframework.jdbc.core.JdbcTemplate(dataSource);
            jdbc.update("insert into procedimento (numero_procedimento) select g from generate_series(20000, 20249) g");
            jdbc.update("insert into procedimento_sexo (procedimento_id, sexo) select id, 'M' from procedimento where numero_procedimento >= 20000");
            jdbc.update("insert into procedimento_faixa_etaria (procedimento_id, faixa) select id, 'CRIANCA' from procedimento where numero_procedimento >= 20000");
            long comPaginaCheia = sqlDaListagem(estatisticas, 200);
            // página + count + sexos + faixas (em lote); sem N+1 mesmo na página máxima
            assertThat(comPaginaCheia).as("sem N+1 na página de 200 itens").isEqualTo(4);
        } finally {
            estatisticas.setStatisticsEnabled(false);
        }
    }

    private long sqlDaListagem(Statistics estatisticas) throws Exception {
        return sqlDaListagem(estatisticas, 50);
    }

    private long sqlDaListagem(Statistics estatisticas, int tamanho) throws Exception {
        estatisticas.clear();
        mvc.perform(get(BASE).header(HttpHeaders.AUTHORIZATION, JwtTestSupport.cabecalhoBearer(1L, "t@ex.com"))
                .param("size", String.valueOf(tamanho))).andExpect(status().isOk());
        return estatisticas.getPrepareStatementCount();
    }
}
