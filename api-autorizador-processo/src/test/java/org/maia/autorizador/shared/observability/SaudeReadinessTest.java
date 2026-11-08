package org.maia.autorizador.shared.observability;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Probes de saúde: {@code liveness} sempre 200; {@code readiness} 200 com o banco ativo e 503 com o banco parado.
 * Usa um PostgreSQL próprio (não o compartilhado da JVM) porque o teste para o contêiner. Roda sem perfil
 * {@code local}: a resposta não traz detalhes.
 */
@SpringBootTest(properties = "spring.datasource.hikari.connection-timeout=2000")
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SaudeReadinessTest {

    @ServiceConnection
    static final PostgreSQLContainer POSTGRES_ISOLADO = new PostgreSQLContainer("postgres:16-alpine");

    static {
        POSTGRES_ISOLADO.start();
    }

    @Autowired
    MockMvc mvc;

    @Test
    void readinessEhUpComBancoAtivoEDownComBancoParado() throws Exception {
        mvc.perform(get("/actuator/health/liveness")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        mvc.perform(get("/actuator/health/readiness")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        POSTGRES_ISOLADO.stop();

        mvc.perform(get("/actuator/health/readiness")).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"));
        // O processo continua vivo: liveness não depende do banco.
        mvc.perform(get("/actuator/health/liveness")).andExpect(status().isOk());
    }
}
