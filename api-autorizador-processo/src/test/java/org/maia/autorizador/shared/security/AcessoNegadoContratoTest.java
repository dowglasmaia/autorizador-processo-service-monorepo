package org.maia.autorizador.shared.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.maia.autorizador.contrato.ContratoOpenApi;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code ACESSO_NEGADO} (403) não é alcançável por nenhuma rota atual (não há papéis/perfis), então o manipulador é
 * exercitado diretamente e a resposta é validada contra o esquema {@code ProblemDetail} do contrato.
 */
class AcessoNegadoContratoTest {

    @Test
    void respostaDeAcessoNegadoRespeitaOContrato() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/v1/painel/resumo");
        var response = new MockHttpServletResponse();

        new ProblemAccessDeniedHandler().handle(request, response, new AccessDeniedException("negado"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).startsWith("application/problem+json");
        Object corpo = new JsonMapper().readValue(response.getContentAsString(java.nio.charset.StandardCharsets.UTF_8),
                Object.class);
        assertThat(((Map<?, ?>) corpo).get("code")).isEqualTo("ACESSO_NEGADO");
        assertThat(new ContratoOpenApi().validarContraEsquemaComum("ProblemDetail", corpo)).isEmpty();
    }
}
