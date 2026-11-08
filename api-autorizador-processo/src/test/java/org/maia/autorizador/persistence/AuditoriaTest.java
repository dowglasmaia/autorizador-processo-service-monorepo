package org.maia.autorizador.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.maia.autorizador.support.IntegrationTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionTemplate;
import spike.auditoria.SpikeProcedimento;

/** Usa a entidade mínima {@code @Audited} do spike (ADR-05) até existir a entidade real do módulo. */
@Import(AuditoriaTest.Mapeamento.class)
class AuditoriaTest extends IntegrationTestBase {

    @TestConfiguration
    @EntityScan(basePackageClasses = SpikeProcedimento.class,
            basePackages = "org.maia.autorizador.autenticacao.infrastructure.persistence")
    static class Mapeamento {
    }

    @Autowired
    EntityManager em;
    @Autowired
    TransactionTemplate tx;

    @Test
    void insertGeraRevisaoEAuditoria() {
        tx.executeWithoutResult(s -> em.persist(new SpikeProcedimento(4321)));

        assertThat(jdbc.queryForObject("select count(*) from revinfo", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("""
                select count(*) from procedimento_audit a join revinfo r on r.rev = a.rev
                where a.numero_procedimento = 4321 and a.revtype = 0 and r.revtstmp is not null
                """, Integer.class)).isEqualTo(1);
    }
}
