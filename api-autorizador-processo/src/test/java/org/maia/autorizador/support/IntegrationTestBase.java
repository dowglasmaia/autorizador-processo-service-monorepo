package org.maia.autorizador.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Base dos testes de integração: um único PostgreSQL 16 real (Testcontainers) compartilhado por toda a JVM de
 * testes. O contêiner é iniciado uma vez e encerrado pelo Ryuk ao fim da execução; nunca se usa H2.
 * O banco é limpo antes de cada teste (as migrações do Flyway permanecem aplicadas).
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    @ServiceConnection
    protected static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @Autowired
    protected JdbcTemplate jdbc;

    @BeforeEach
    void limparBancoAntesDoTeste() {
        new LimpezaBanco(jdbc).limpar();
    }
}
