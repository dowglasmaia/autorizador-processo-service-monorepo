package org.maia.autorizador.shared.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.maia.autorizador.autenticacao.application.port.HashSenha;
import org.maia.autorizador.autenticacao.application.port.UsuarioRepository;
import org.maia.autorizador.autenticacao.domain.model.Usuario;
import org.maia.autorizador.support.IntegrationTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.http.MediaType;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.web.servlet.MockMvc;

/** Perfil padrão: o log sai em JSON ECS com correlação e sem dado sensível numa requisição de login. */
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class LogJsonEcsTest extends IntegrationTestBase {

    private static final String SENHA = "senha-secreta-para-log-123";

    @Autowired
    MockMvc mvc;

    @Autowired
    UsuarioRepository usuarios;

    @Autowired
    HashSenha hashSenha;

    private LoggingSystem sistemaDeLog() {
        return LoggingSystem.get(getClass().getClassLoader());
    }

    private void iniciarLog(String formato) {
        var env = new MockEnvironment().withProperty("logging.structured.format.console", formato)
                .withProperty("logging.structured.ecs.service.name", "api-autorizador-processo")
                .withProperty("logging.structured.ecs.service.environment", "production");
        sistemaDeLog().cleanUp();
        sistemaDeLog().initialize(new LoggingInitializationContext(env), null, null);
    }

    // O LoggingSystem é um singleton da JVM já iniciado por outro contexto: reinicia só neste teste e restaura.
    @BeforeEach
    void ativarEcs() {
        iniciarLog("ecs");
    }

    @AfterEach
    void restaurarLog() {
        iniciarLog("");
    }

    @Test
    void loginGeraLogJsonEcsComCorrelacaoESemDadoSensivel(CapturedOutput saida) throws Exception {
        String email = "log" + UUID.randomUUID().toString().substring(0, 8) + "@ex.com";
        usuarios.salvar(Usuario.novo(email, hashSenha.gerar(SENHA)));
        String requestId = UUID.randomUUID().toString();
        String corpo = "{\"email\":\"" + email + "\",\"senha\":\"" + SENHA + "\"}";

        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(corpo)
                .header("X-Request-Id", requestId).with(r -> {
                    r.setRemoteAddr("10.9.0.1");
                    return r;
                })).andReturn();

        String linha = saida.getAll().lines().filter(l -> l.contains("request completed")).findFirst()
                .orElseThrow(() -> new AssertionError("log de acesso não encontrado:\n" + saida.getAll()));

        assertThat(linha).startsWith("{").endsWith("}");
        assertThat(linha).contains("\"@timestamp\"", "\"log\":{\"level\":\"INFO\"", "\"ecs\":{\"version\"");
        assertThat(linha).contains(requestId);
        assertThat(linha).containsPattern("\"traceId\":\"[0-9a-f]{32}\"");

        String tudo = saida.getAll();
        assertThat(tudo).doesNotContain(SENHA).doesNotContain(email).doesNotContain("eyJ");
    }
}
