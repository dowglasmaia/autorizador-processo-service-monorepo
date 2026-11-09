package org.maia.autorizador.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.maia.autorizador.procedimento.application.port.ProcedimentoRepository;
import org.maia.autorizador.procedimento.domain.model.Procedimento;
import org.maia.autorizador.procedimento.domain.model.FaixaEtaria;
import org.maia.autorizador.procedimento.domain.model.Sexo;
import org.maia.autorizador.support.IntegrationTestBase;
import org.springframework.beans.factory.annotation.Autowired;

/** Auditoria Envers (RN10, ADR-05) da entidade real do módulo {@code procedimento}, nas tabelas da V2. */
class AuditoriaTest extends IntegrationTestBase {

    @Autowired
    ProcedimentoRepository procedimentos;

    @Test
    void insertGeraRevisaoEAuditoriaComAsColecoes() {
        Procedimento salvo = procedimentos.salvar(Procedimento.create(4321, List.of(Sexo.M, Sexo.F), List.of(FaixaEtaria.CRIANCA, FaixaEtaria.ADOLESCENTE, FaixaEtaria.ADULTO)));

        assertThat(jdbc.queryForObject("select count(*) from revinfo", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("""
                select count(*) from procedimento_audit a join revinfo r on r.rev = a.rev
                where a.id = ? and a.numero_procedimento = 4321 and a.revtype = 0 and r.revtstmp is not null
                """, Integer.class, salvo.id())).isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "select count(*) from procedimento_sexo_audit where procedimento_id = ? and revtype = 0",
                Integer.class, salvo.id())).isEqualTo(2);
        assertThat(jdbc.queryForObject(
                "select count(*) from procedimento_faixa_etaria_audit where procedimento_id = ? and revtype = 0",
                Integer.class, salvo.id())).isEqualTo(3);
    }
}
