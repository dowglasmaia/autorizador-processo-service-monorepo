package org.maia.autorizador.contrato;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.jupiter.api.Test;
import org.maia.autorizador.painel.application.usecase.ObterResumoPainelUseCase;
import org.maia.autorizador.support.IntegrationTestBase;
import org.maia.autorizador.support.JwtTestSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

/** {@code ERRO_INTERNO} (500) real: uma falha inesperada num caso de uso, com a cadeia completa de filtros. */
@AutoConfigureMockMvc
class ContratoErroInternoApiTest extends IntegrationTestBase {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ObterResumoPainelUseCase resumo;

    @Test
    void falhaInesperadaRespeitaOContratoComCodeErroInterno() throws Exception {
        when(resumo.executar()).thenThrow(new IllegalStateException("falha simulada"));

        var r = mvc.perform(get("/api/v1/painel/resumo").header(HttpHeaders.AUTHORIZATION,
                JwtTestSupport.cabecalhoBearer(1L, "t@ex.com"))).andReturn().getResponse();

        Object corpo = new JsonMapper().readValue(r.getContentAsString(java.nio.charset.StandardCharsets.UTF_8),
                Object.class);
        assertThat(new ContratoOpenApi().validarResposta("get", "/painel/resumo", r.getStatus(),
                r.getContentType(), java.util.Map.of(), corpo)).isEmpty();
        assertThat(r.getStatus()).isEqualTo(500);
        assertThat(((java.util.Map<?, ?>) corpo).get("code")).isEqualTo("ERRO_INTERNO");
    }
}
