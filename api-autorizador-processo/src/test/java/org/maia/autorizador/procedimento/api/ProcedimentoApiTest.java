package org.maia.autorizador.procedimento.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Catálogo de procedimentos ponta a ponta (JWT de teste, segurança, controller, casos de uso e PostgreSQL real). */
@AutoConfigureMockMvc
class ProcedimentoApiTest extends IntegrationTestBase {

    private static final String BASE = "/api/v1/procedimentos";

    @Autowired
    MockMvc mvc;

    @Autowired
    DataSource dataSource;

    @BeforeEach
    void semear() {
        new ResourceDatabasePopulator(new ClassPathResource("db/seed/V1000__seed_local.sql"))
                .execute(dataSource);
    }

    private static MockHttpServletRequestBuilder comToken(MockHttpServletRequestBuilder req) {
        return req.header(HttpHeaders.AUTHORIZATION, JwtTestSupport.cabecalhoBearer(1L, "teste@ex.com"));
    }

    private ResultActions listar() throws Exception {
        return mvc.perform(comToken(get(BASE)));
    }

    private ResultActions cadastrar(String json) throws Exception {
        return mvc.perform(comToken(post(BASE)).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void listagemDoSeedTemTresItensComIdadesAscendentes() throws Exception {
        listar().andExpect(status().isOk())
                .andExpect(jsonPath("$.itens", hasSize(3)))
                .andExpect(jsonPath("$.totalItens").value(3))
                .andExpect(jsonPath("$.totalPaginas").value(1))
                .andExpect(jsonPath("$.itens[0].numProcedimento").value(1234))
                .andExpect(jsonPath("$.itens[0].sexos[0]").value("F"))
                .andExpect(jsonPath("$.itens[0].idades[0]").value(10))
                .andExpect(jsonPath("$.itens[0].idades[1]").value(20))
                .andExpect(jsonPath("$.itens[1].numProcedimento").value(4567))
                .andExpect(jsonPath("$.itens[1].sexos[0]").value("M"))
                .andExpect(jsonPath("$.itens[1].sexos[1]").value("F"))
                .andExpect(jsonPath("$.itens[2].numProcedimento").value(6789));
    }

    @Test
    void listagemFiltraPorPrefixoOrdenaEPagina() throws Exception {
        mvc.perform(comToken(get(BASE)).param("q", "45")).andExpect(status().isOk())
                .andExpect(jsonPath("$.itens", hasSize(1)))
                .andExpect(jsonPath("$.itens[0].numProcedimento").value(4567));
        mvc.perform(comToken(get(BASE)).param("sort", "numProcedimento,desc").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].numProcedimento").value(6789))
                .andExpect(jsonPath("$.itens", hasSize(2)))
                .andExpect(jsonPath("$.totalItens").value(3))
                .andExpect(jsonPath("$.totalPaginas").value(2));
    }

    @Test
    void cadastroSeguidoDeConsultaDevolveOMesmoProcedimento() throws Exception {
        String json = cadastrar("{\"numProcedimento\":9999,\"sexos\":[\"f\",\"M\"],\"idades\":[30,18]}")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.startsWith(BASE + "/")))
                .andExpect(jsonPath("$.sexos[0]").value("M"))
                .andExpect(jsonPath("$.sexos[1]").value("F"))
                .andExpect(jsonPath("$.idades[0]").value(18))
                .andExpect(jsonPath("$.idades[1]").value(30))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        int id = com.jayway.jsonpath.JsonPath.read(json, "$.id");

        mvc.perform(comToken(get(BASE + "/" + id))).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.numProcedimento").value(9999))
                .andExpect(jsonPath("$.sexos[1]").value("F"))
                .andExpect(jsonPath("$.idades[1]").value(30))
                .andExpect(jsonPath("$.criadoEm").exists());
        listar().andExpect(jsonPath("$.totalItens").value(4));
    }

    @Test
    void cadastroComListasVaziasEhAceito() throws Exception {
        cadastrar("{\"numProcedimento\":55,\"sexos\":[],\"idades\":[]}").andExpect(status().isCreated())
                .andExpect(jsonPath("$.sexos", hasSize(0)))
                .andExpect(jsonPath("$.idades", hasSize(0)));
    }

    @Test
    void numeroRepetidoRetorna403DuplicadoESeedContinuaIntacto() throws Exception {
        cadastrar("{\"numProcedimento\":1234,\"sexos\":[\"M\"],\"idades\":[1]}").andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PROCEDIMENTO_DUPLICADO"));
        cadastrar("{\"numProcedimento\":9999}").andExpect(status().isCreated());
        cadastrar("{\"numProcedimento\":9999}").andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PROCEDIMENTO_DUPLICADO"));

        listar().andExpect(jsonPath("$.totalItens").value(4));
        mvc.perform(comToken(get(BASE)).param("q", "1234")).andExpect(jsonPath("$.itens[0].idades[1]").value(20));
    }

    @Test
    void entradaInvalidaRetorna400SemQuebrarAListagem() throws Exception {
        cadastrar("{\"numProcedimento\":1,\"sexos\":[\"X\"]}").andExpect(status().isBadRequest());
        cadastrar("{\"numProcedimento\":0}").andExpect(status().isBadRequest());
        cadastrar("{\"numProcedimento\":1,\"idades\":[151]}").andExpect(status().isBadRequest());
        cadastrar("{\"numProcedimento\":").andExpect(status().isBadRequest());

        listar().andExpect(status().isOk()).andExpect(jsonPath("$.totalItens").value(3));
        assertThat(jdbc.queryForObject("select count(*) from procedimento", Integer.class)).isEqualTo(3);
    }

    @Test
    void obterInexistenteRetorna404() throws Exception {
        mvc.perform(comToken(get(BASE + "/987654"))).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROCEDIMENTO_NAO_ENCONTRADO"));
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mvc.perform(get(BASE)).andExpect(status().isUnauthorized());
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("{\"numProcedimento\":1}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get(BASE + "/1")).andExpect(status().isUnauthorized());
    }
}
