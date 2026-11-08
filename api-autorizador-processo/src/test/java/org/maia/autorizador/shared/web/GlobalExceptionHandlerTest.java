package org.maia.autorizador.shared.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(controllers = ErroTesteController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, CorsConfig.class})
@ActiveProfiles("local")
class GlobalExceptionHandlerTest {

    private static final String BASE = "/api/v1/teste-erros";

    @Autowired
    private MockMvc mvc;

    private static void problema(ResultActions r, int status, String code) throws Exception {
        r.andExpect(status().is(status))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.instance").exists())
                .andExpect(jsonPath("$.timestamp").exists());
        if (code != null) {
            r.andExpect(jsonPath("$.code").value(code));
        }
    }

    @Test
    void validacaoDeCorpoRetornaErrosPorCampo() throws Exception {
        var r = mvc.perform(post(BASE + "/validacao").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"\"}"));
        problema(r, 400, "VALIDACAO_INVALIDA");
        r.andExpect(jsonPath("$.errors[0].campo").value("nome"))
                .andExpect(jsonPath("$.errors[0].mensagem").value("não pode ser vazio"));
    }

    @Test
    void validacaoDeParametroRetornaErrors() throws Exception {
        var r = mvc.perform(get(BASE + "/restricao").param("pagina", "0"));
        problema(r, 400, "VALIDACAO_INVALIDA");
        r.andExpect(jsonPath("$.errors[0].campo").value("pagina"))
                .andExpect(jsonPath("$.errors[0].mensagem").value("deve ser maior que zero"));
    }

    @Test
    void jsonMalformadoRetornaCorpoInvalido() throws Exception {
        problema(mvc.perform(post(BASE + "/validacao").contentType(MediaType.APPLICATION_JSON).content("{nome")),
                400, "CORPO_INVALIDO");
    }

    @Test
    void parametroComTipoInvalido() throws Exception {
        problema(mvc.perform(get(BASE + "/parametro-tipo").param("numero", "abc")), 400, "PARAMETRO_INVALIDO");
    }

    @Test
    void parametroAusente() throws Exception {
        problema(mvc.perform(get(BASE + "/parametro-ausente")), 400, "PARAMETRO_INVALIDO");
    }

    @Test
    void regraDeNegocioProibida() throws Exception {
        problema(mvc.perform(get(BASE + "/negocio-proibido")), 403, "OPERACAO_NAO_PERMITIDA");
    }

    @Test
    void regraDeNegocioInexistente() throws Exception {
        problema(mvc.perform(get(BASE + "/negocio-inexistente")), 404, "PROCEDIMENTO_NAO_ENCONTRADO");
    }

    @Test
    void rotaInexistenteRetorna404Generico() throws Exception {
        problema(mvc.perform(get("/api/v1/nao-existe")), 404, null);
    }

    @Test
    void erroInesperadoNaoVazaDetalhesInternos() throws Exception {
        var r = mvc.perform(get(BASE + "/inesperado"));
        problema(r, 500, "ERRO_INTERNO");
        r.andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.stackTrace").doesNotExist())
                .andExpect(content().string(not(containsString("IllegalStateException"))))
                .andExpect(content().string(not(containsString("org.maia"))))
                .andExpect(content().string(not(containsString("tabela_secreta"))))
                .andExpect(content().string(not(containsString("select"))));
    }

    @Test
    void preflightDeOrigemPermitidaNoPerfilLocal() throws Exception {
        mvc.perform(options(BASE + "/negocio-proibido")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("GET")));
    }

    @Test
    void preflightDeOrigemNegadaRetorna403() throws Exception {
        mvc.perform(options(BASE + "/negocio-proibido")
                        .header("Origin", "http://malicioso.example")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
