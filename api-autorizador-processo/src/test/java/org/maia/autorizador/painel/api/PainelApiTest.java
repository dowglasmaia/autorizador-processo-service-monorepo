package org.maia.autorizador.painel.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.sql.DataSource;
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

/** Painel ponta a ponta (JWT de teste, segurança, controller, casos de uso e PostgreSQL real). */
@AutoConfigureMockMvc
class PainelApiTest extends IntegrationTestBase {

    private static final String RESUMO = "/api/v1/painel/resumo";

    @Autowired
    MockMvc mvc;

    @Autowired
    DataSource dataSource;

    @BeforeEach
    void semear() {
        new ResourceDatabasePopulator(new ClassPathResource("db/seed/V1000__seed_local.sql")).execute(dataSource);
    }

    private org.springframework.test.web.servlet.ResultActions resumo() throws Exception {
        return mvc.perform(get(RESUMO).header(HttpHeaders.AUTHORIZATION, JwtTestSupport.cabecalhoBearer(1L, "t@ex.com")));
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mvc.perform(get(RESUMO)).andExpect(status().isUnauthorized());
    }

    @Test
    void seedSemAutorizacoes() throws Exception {
        resumo().andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProcedimentos").value(3))
                .andExpect(jsonPath("$.totalAutorizacoes").value(0))
                .andExpect(jsonPath("$.idadesCobertas").value(3))
                .andExpect(jsonPath("$.coberturaPorIdade", hasSize(3)))
                .andExpect(jsonPath("$.coberturaPorIdade[0].idade").value(10))
                .andExpect(jsonPath("$.coberturaPorIdade[0].quantidade").value(2))
                .andExpect(jsonPath("$.coberturaPorIdade[2].idade").value(30))
                .andExpect(jsonPath("$.coberturaPorIdade[2].quantidade").value(1))
                .andExpect(jsonPath("$.distribuicaoSexos.masculino").value(2))
                .andExpect(jsonPath("$.distribuicaoSexos.feminino").value(3))
                .andExpect(jsonPath("$.ultimasAutorizacoes", hasSize(0)));
    }

    @Test
    void aposUmaAutorizacaoCriada() throws Exception {
        mvc.perform(post("/api/v1/autorizacoes").header(HttpHeaders.AUTHORIZATION,
                JwtTestSupport.cabecalhoBearer(1L, "t@ex.com")).contentType(MediaType.APPLICATION_JSON)
                .content("{\"numProcedimento\":4567,\"idade\":20,\"sexo\":\"F\"}")).andExpect(status().isCreated());

        resumo().andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAutorizacoes").value(1))
                .andExpect(jsonPath("$.ultimasAutorizacoes", hasSize(1)))
                .andExpect(jsonPath("$.ultimasAutorizacoes[0].permitido").value(true))
                .andExpect(jsonPath("$.ultimasAutorizacoes[0].criadaEm").exists())
                .andExpect(jsonPath("$.ultimasAutorizacoes[0].procedimento.numProcedimento").value(4567));
    }
}
