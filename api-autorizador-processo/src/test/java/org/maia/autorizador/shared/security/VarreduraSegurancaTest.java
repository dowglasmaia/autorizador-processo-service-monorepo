package org.maia.autorizador.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.maia.autorizador.support.IntegrationTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Varre todos os mapeamentos MVC: qualquer endpoint que não seja público deve responder {@code 401} sem token
 * (SEC-13..SEC-20). Também confere que senha e token não vazam em logs nem em corpos de erro.
 */
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class VarreduraSegurancaTest extends IntegrationTestBase {

    /** Únicos endpoints públicos (método + caminho). */
    private static final Set<String> PUBLICOS = Set.of("POST /api/v1/auth/login");

    @Autowired
    MockMvc mvc;

    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("requestMappingHandlerMapping")
    RequestMappingHandlerMapping mapeamentos;

    @Test
    void todoEndpointNaoPublicoRetorna401SemToken() throws Exception {
        List<String> violacoes = new ArrayList<>();
        int verificados = 0;
        for (RequestMappingInfo info : mapeamentos.getHandlerMethods().keySet()) {
            var padroes = info.getPathPatternsCondition() == null ? Set.<String>of()
                    : info.getPathPatternsCondition().getPatternValues();
            Set<RequestMethod> metodos = info.getMethodsCondition().getMethods();
            for (String padrao : padroes) {
                for (RequestMethod metodo : metodos.isEmpty() ? Set.of(RequestMethod.GET) : metodos) {
                    if (PUBLICOS.contains(metodo + " " + padrao)) {
                        continue;
                    }
                    verificados++;
                    String caminho = padrao.replaceAll("\\{[^}]+}", "1");
                    int status = mvc.perform(request(HttpMethod.valueOf(metodo.name()), caminho)
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                            .andReturn().getResponse().getStatus();
                    if (status != 401) {
                        violacoes.add(metodo + " " + padrao + " respondeu " + status);
                    }
                }
            }
        }
        assertThat(verificados).as("endpoints verificados").isPositive();
        assertThat(violacoes).as("endpoints não públicos sem 401").isEmpty();
    }

    @Test
    void senhaETokenNaoAparecemEmLogsNemEmErros(CapturedOutput saida) throws Exception {
        String senha = "senha-secreta-unica-98765";
        String corpo = "{\"email\":\"vazamento@ex.com\",\"senha\":\"" + senha + "\"}";

        String erro = mvc.perform(request(HttpMethod.POST, "/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andReturn().getResponse().getContentAsString();
        String token = org.maia.autorizador.support.JwtTestSupport.tokenValido(1L, "vazamento@ex.com");
        String erroToken = mvc.perform(request(HttpMethod.GET, "/api/v1/auth/me")
                .header("Authorization", "Bearer " + token + "x"))
                .andReturn().getResponse().getContentAsString();

        assertThat(erro).doesNotContain(senha);
        assertThat(erroToken).doesNotContain(token);
        assertThat(saida.getAll()).doesNotContain(senha).doesNotContain(token)
                .doesNotContain("vazamento@ex.com");
    }
}
