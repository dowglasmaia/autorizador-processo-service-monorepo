package org.maia.autorizador.shared.docs;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.maia.autorizador.support.IntegrationTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** No perfil {@code local} a página e o contrato estático de {@code openapi/} são servidos sem autenticação. */
@AutoConfigureMockMvc
@ActiveProfiles("local")
class DocumentacaoPerfilLocalTest extends IntegrationTestBase {

    @Autowired
    MockMvc mvc;

    @Test
    void servePaginaEContratoEstatico() throws Exception {
        mvc.perform(get("/swagger-ui.html")).andExpect(status().isOk())
                .andExpect(content().string(containsString("swagger-ui")));
        mvc.perform(get("/v3/api-docs/modules/auth.yaml")).andExpect(status().isOk())
                .andExpect(content().string(containsString("openapi: 3.1.0")));
        mvc.perform(get("/v3/api-docs/common/responses.yaml")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs/modules/inexistente.yaml")).andExpect(status().isNotFound());
    }
}
