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

    @Autowired
    org.maia.autorizador.procedimento.application.port.ProcedimentoRepository procedimentos;

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
                .andExpect(jsonPath("$.itens[0].faixasEtarias[0]").value("CRIANCA"))
                .andExpect(jsonPath("$.itens[0].faixasEtarias[1]").value("ADOLESCENTE"))
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
        String json = cadastrar("{\"nome\":\"Proc\",\"categoria\":\"CONSULTAS\",\"numProcedimento\":9999,\"sexos\":[\"f\",\"M\"],\"faixasEtarias\":[\"ADULTO\",\"ADOLESCENTE\"]}")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.startsWith(BASE + "/")))
                .andExpect(jsonPath("$.sexos[0]").value("M"))
                .andExpect(jsonPath("$.sexos[1]").value("F"))
                .andExpect(jsonPath("$.faixasEtarias[0]").value("ADOLESCENTE"))
                .andExpect(jsonPath("$.faixasEtarias[1]").value("ADULTO"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        int id = com.jayway.jsonpath.JsonPath.read(json, "$.id");

        mvc.perform(comToken(get(BASE + "/" + id))).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.numProcedimento").value(9999))
                .andExpect(jsonPath("$.sexos[1]").value("F"))
                .andExpect(jsonPath("$.faixasEtarias[1]").value("ADULTO"))
                .andExpect(jsonPath("$.criadoEm").exists());
        listar().andExpect(jsonPath("$.totalItens").value(4));
    }

    @Test
    void cadastroEstendidoEConsultaDevolvemOsMesmosValores() throws Exception {
        String json = cadastrar("""
                {"numProcedimento":8888,"nome":"  Ressonância  ","codigoTuss":"40101010",
                 "categoria":"EXAMES_IMAGEM","especialidade":"NEUROLOGIA","tipoCobertura":"REGRAS",
                 "quantidadeMaxima":2,"periodo":"ANO","exigeAutorizacaoPrevia":true,
                 "sexos":["F"],"faixasEtarias":["ADULTO"]}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Ressonância"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        int id = com.jayway.jsonpath.JsonPath.read(json, "$.id");

        mvc.perform(comToken(get(BASE + "/" + id))).andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ressonância"))
                .andExpect(jsonPath("$.codigoTuss").value("40101010"))
                .andExpect(jsonPath("$.categoria").value("EXAMES_IMAGEM"))
                .andExpect(jsonPath("$.especialidade").value("NEUROLOGIA"))
                .andExpect(jsonPath("$.tipoCobertura").value("REGRAS"))
                .andExpect(jsonPath("$.quantidadeMaxima").value(2))
                .andExpect(jsonPath("$.periodo").value("ANO"))
                .andExpect(jsonPath("$.exigeAutorizacaoPrevia").value(true))
                .andExpect(jsonPath("$.exigeJustificativaMedica").value(false));
        mvc.perform(comToken(get(BASE)).param("q", "8888")).andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].categoria").value("EXAMES_IMAGEM"))
                .andExpect(jsonPath("$.itens[0].periodo").value("ANO"));
    }

    @Test
    void padroesSaoAplicadosQuandoOmitidos() throws Exception {
        cadastrar("{\"numProcedimento\":8889,\"nome\":\"Consulta\",\"categoria\":\"CONSULTAS\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoCobertura").value("PLANO"))
                .andExpect(jsonPath("$.exigeAutorizacaoPrevia").value(false))
                .andExpect(jsonPath("$.exigeJustificativaMedica").value(false))
                .andExpect(jsonPath("$.quantidadeMaxima").doesNotExist())
                .andExpect(jsonPath("$.periodo").doesNotExist());
    }

    @Test
    void camposInvalidosRetornam400ComErroPorCampoENadaEGravado() throws Exception {
        cadastrar("{\"numProcedimento\":8890,\"categoria\":\"CONSULTAS\"}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACAO_INVALIDA"))
                .andExpect(jsonPath("$.errors[0].campo").value("nome"));
        cadastrar("{\"numProcedimento\":8890,\"nome\":\"x\"}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].campo").value("categoria"));
        cadastrar("{\"numProcedimento\":8890,\"nome\":\"x\",\"categoria\":\"CONSULTAS\",\"quantidadeMaxima\":2}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors[0].campo").value("periodo"));
        cadastrar("{\"numProcedimento\":8890,\"nome\":\"x\",\"categoria\":\"CONSULTAS\",\"periodo\":\"MES\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].campo").value("quantidadeMaxima"));

        assertThat(jdbc.queryForObject("select count(*) from procedimento", Integer.class)).isEqualTo(3);
    }

    @Test
    void procedimentoDoSeedTemCamposNovosNulosEADecisaoSegueSoSexoEIdade() throws Exception {
        mvc.perform(comToken(get(BASE)).param("q", "1234")).andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].nome").doesNotExist())
                .andExpect(jsonPath("$.itens[0].categoria").doesNotExist())
                .andExpect(jsonPath("$.itens[0].tipoCobertura").doesNotExist())
                .andExpect(jsonPath("$.itens[0].exigeAutorizacaoPrevia").doesNotExist());

        // O módulo autorizacao só chega na etapa 10; a regra de decisão é a do domínio, que ignora o catálogo.
        var seed = procedimentos.buscarPorNumero(1234).orElseThrow();
        assertThat(seed.catalogo()).isEqualTo(org.maia.autorizador.procedimento.domain.model.DadosCatalogo.VAZIO);
        assertThat(seed.avaliar(10, org.maia.autorizador.procedimento.domain.model.Sexo.F)).isTrue();
        assertThat(seed.avaliar(20, org.maia.autorizador.procedimento.domain.model.Sexo.F)).isTrue();
        assertThat(seed.avaliar(10, org.maia.autorizador.procedimento.domain.model.Sexo.M)).isFalse();
        assertThat(seed.avaliar(15, org.maia.autorizador.procedimento.domain.model.Sexo.F)).isTrue();
        assertThat(seed.avaliar(30, org.maia.autorizador.procedimento.domain.model.Sexo.F)).isFalse();
    }

    @Test
    void cadastroComListasVaziasEhAceito() throws Exception {
        cadastrar("{\"nome\":\"Proc\",\"categoria\":\"CONSULTAS\",\"numProcedimento\":55,\"sexos\":[],\"faixasEtarias\":[]}").andExpect(status().isCreated())
                .andExpect(jsonPath("$.sexos", hasSize(0)))
                .andExpect(jsonPath("$.faixasEtarias", hasSize(0)));
    }

    @Test
    void numeroRepetidoRetorna403DuplicadoESeedContinuaIntacto() throws Exception {
        cadastrar("{\"nome\":\"Proc\",\"categoria\":\"CONSULTAS\",\"numProcedimento\":1234,\"sexos\":[\"M\"],\"faixasEtarias\":[\"CRIANCA\"]}").andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PROCEDIMENTO_DUPLICADO"));
        cadastrar("{\"nome\":\"Proc\",\"categoria\":\"CONSULTAS\",\"numProcedimento\":9999}").andExpect(status().isCreated());
        cadastrar("{\"nome\":\"Proc\",\"categoria\":\"CONSULTAS\",\"numProcedimento\":9999}").andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PROCEDIMENTO_DUPLICADO"));

        listar().andExpect(jsonPath("$.totalItens").value(4));
        mvc.perform(comToken(get(BASE)).param("q", "1234")).andExpect(jsonPath("$.itens[0].faixasEtarias[1]").value("ADOLESCENTE"));
    }

    @Test
    void entradaInvalidaRetorna400SemQuebrarAListagem() throws Exception {
        cadastrar("{\"numProcedimento\":1,\"sexos\":[\"X\"]}").andExpect(status().isBadRequest());
        cadastrar("{\"numProcedimento\":0}").andExpect(status().isBadRequest());
        cadastrar("{\"numProcedimento\":1,\"faixasEtarias\":[\"BEBE\"]}").andExpect(status().isBadRequest());
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
