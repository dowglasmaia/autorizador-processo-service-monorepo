package org.maia.autorizador.shared.docs;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.maia.autorizador.support.IntegrationTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** Fora do perfil {@code local} a documentação da API não existe: as rotas respondem 404. */
@AutoConfigureMockMvc
class DocumentacaoPerfilPadraoTest extends IntegrationTestBase {

    @Autowired
    MockMvc mvc;

    @Test
    void rotasDeDocumentacaoRespondem404() throws Exception {
        for (String caminho : new String[] {"/swagger-ui.html", "/swagger-ui/index.html", "/v3/api-docs",
            "/v3/api-docs/modules/auth.yaml"}) {
            mvc.perform(get(caminho)).andExpect(status().isNotFound());
        }
    }
}
