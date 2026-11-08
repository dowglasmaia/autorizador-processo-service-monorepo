package spike.auditoria;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Spike da ADR-05: o Envers do Hibernate efetivo do Boot 4.1.2 funciona em Java 25 com PostgreSQL 16 e grava
 * nas tabelas criadas por {@code V2__auditoria.sql}?
 */
@SpringBootTest(classes = EnversSpikeTest.Cfg.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class EnversSpikeTest {

    @Configuration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = SpikeProcedimento.class)
    static class Cfg {
    }

    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @Autowired
    EntityManager em;
    @Autowired
    TransactionTemplate tx;
    @Autowired
    JdbcTemplate jdbc;

    @Test
    void enversGravaRevisaoEAuditoriaNasTabelasDaV2() {
        tx.executeWithoutResult(s -> em.persist(new SpikeProcedimento(9999)));

        assertThat(jdbc.queryForObject("select count(*) from revinfo", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "select count(*) from procedimento_audit where numero_procedimento = 9999 and revtype = 0",
                Integer.class)).isEqualTo(1);
    }
}
