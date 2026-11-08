package org.maia.autorizador.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.maia.autorizador.support.IntegrationTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** SEC-03: corpo acima do limite configurável é recusado com 413 em problem+json, mesmo sem token. */
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.http.max-body-bytes=1024")
class LimiteTamanhoRequisicaoTest extends IntegrationTestBase {

    @Autowired
    MockMvc mvc;

    @Test
    void corpoAcimaDoLimiteRetorna413ProblemJson() throws Exception {
        String corpo = "{\"email\":\"" + "a".repeat(2000) + "@ex.com\",\"senha\":\"x\"}";
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(413))
                .andExpect(jsonPath("$.code").value("CORPO_INVALIDO"));
    }

    @Test
    void corpoDentroDoLimiteSegueOFluxoNormal() throws Exception {
        String corpo = "{\"email\":\"naoexiste@ex.com\",\"senha\":\"senha-qualquer\"}";
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void corpoSemTamanhoDeclaradoRetorna411() throws Exception {
        var filtro = new LimiteTamanhoRequisicaoFilter(1024);
        var request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.addHeader("Transfer-Encoding", "chunked");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filtro.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(411);
        assertThat(chain.getRequest()).isNull();
    }
}
