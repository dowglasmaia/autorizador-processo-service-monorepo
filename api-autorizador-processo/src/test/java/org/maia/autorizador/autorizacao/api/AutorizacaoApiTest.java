package org.maia.autorizador.autorizacao.api;

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
import org.springframework.test.web.servlet.ResultActions;

/** Autorizações ponta a ponta (JWT de teste, segurança, controller, casos de uso e PostgreSQL real). */
@AutoConfigureMockMvc
class AutorizacaoApiTest extends IntegrationTestBase {

    private static final String BASE = "/api/v1/autorizacoes";

    @Autowired
    MockMvc mvc;

    @Autowired
    DataSource dataSource;

    @BeforeEach
    void semear() {
        new ResourceDatabasePopulator(new ClassPathResource("db/seed/V1000__seed_local.sql")).execute(dataSource);
    }

    private ResultActions solicitar(String json) throws Exception {
        return mvc.perform(post(BASE).header(HttpHeaders.AUTHORIZATION, JwtTestSupport.cabecalhoBearer(1L, "t@ex.com"))
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private ResultActions listar() throws Exception {
        return mvc.perform(get(BASE).header(HttpHeaders.AUTHORIZATION, JwtTestSupport.cabecalhoBearer(1L, "t@ex.com")));
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content("{\"numProcedimento\":4567,\"idade\":20,\"sexo\":\"F\"}")).andExpect(status().isUnauthorized());
        mvc.perform(get(BASE)).andExpect(status().isUnauthorized());
    }

    @Test
    void autorizadoRetorna201EApareceNaListagem() throws Exception {
        solicitar("{\"numProcedimento\":4567,\"idade\":20,\"sexo\":\"F\"}").andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.permitido").value(true))
                .andExpect(jsonPath("$.criadaEm").exists())
                .andExpect(jsonPath("$.procedimento.numProcedimento").value(4567));

        listar().andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItens").value(1))
                .andExpect(jsonPath("$.itens", hasSize(1)))
                .andExpect(jsonPath("$.itens[0].procedimento.numProcedimento").value(4567))
                .andExpect(jsonPath("$.itens[0].permitido").value(true));
    }

    @Test
    void negadoRetorna403SemMudarAContagem() throws Exception {
        solicitar("{\"numProcedimento\":1234,\"idade\":30,\"sexo\":\"M\"}").andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTORIZACAO_NEGADA"));

        listar().andExpect(jsonPath("$.totalItens").value(0));
    }

    @Test
    void procedimentoInexistenteRetorna404() throws Exception {
        solicitar("{\"numProcedimento\":55555,\"idade\":30,\"sexo\":\"M\"}").andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROCEDIMENTO_NAO_ENCONTRADO"));
    }

    @Test
    void sexoPorExtensoRetorna400() throws Exception {
        solicitar("{\"numProcedimento\":4567,\"idade\":20,\"sexo\":\"Masculino\"}").andExpect(status().isBadRequest());
    }

    @Test
    void duasSolicitacoesIguaisGeramDoisRegistrosMaisRecentePrimeiro() throws Exception {
        solicitar("{\"numProcedimento\":4567,\"idade\":20,\"sexo\":\"f\"}").andExpect(status().isCreated());
        solicitar("{\"numProcedimento\":1234,\"idade\":10,\"sexo\":\"F\"}").andExpect(status().isCreated());

        listar().andExpect(jsonPath("$.totalItens").value(2))
                .andExpect(jsonPath("$.itens[0].procedimento.numProcedimento").value(1234))
                .andExpect(jsonPath("$.itens[1].procedimento.numProcedimento").value(4567));
    }
}
