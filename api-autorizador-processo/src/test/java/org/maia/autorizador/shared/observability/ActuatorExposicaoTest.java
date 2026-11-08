package org.maia.autorizador.shared.observability;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.maia.autorizador.support.IntegrationTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** Fora do perfil {@code local}: só health/info; nenhum endpoint de diagnóstico; health sem detalhes. */
@AutoConfigureMockMvc
class ActuatorExposicaoTest extends IntegrationTestBase {

    @Autowired
    MockMvc mvc;

    @Test
    void endpointsDeDiagnosticoNaoSaoExpostos() throws Exception {
        for (String caminho : new String[] {"/actuator/env", "/actuator/metrics", "/actuator/beans"}) {
            // Sem credencial: 401; com a exposição desligada o endpoint nem existe. Em nenhum caso é 200.
            int codigo = mvc.perform(get(caminho)).andReturn().getResponse().getStatus();
            org.junit.jupiter.api.Assertions.assertTrue(codigo == 401 || codigo == 404,
                    caminho + " respondeu " + codigo);
        }
    }

    @Test
    void healthNaoTrazDetalhes() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist())
                .andExpect(jsonPath("$.details").doesNotExist());
    }
}
