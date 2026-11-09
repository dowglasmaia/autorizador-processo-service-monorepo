package org.maia.autorizador.painel.infrastructure;

import org.maia.autorizador.procedimento.domain.model.FaixaEtaria;
import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.maia.autorizador.painel.application.usecase.ObterResumoPainelUseCase;
import org.maia.autorizador.painel.application.usecase.ObterResumoPainelUseCase.Resumo;
import org.maia.autorizador.painel.application.model.CoberturaFaixa;
import org.maia.autorizador.support.IntegrationTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

/** Consultas agregadas do painel contra PostgreSQL real (Testcontainers). */
class PainelConsultaAdapterTest extends IntegrationTestBase {

    @Autowired
    ObterResumoPainelUseCase obterResumo;

    @Autowired
    DataSource dataSource;

    private void semear() {
        new ResourceDatabasePopulator(new ClassPathResource("db/seed/V1000__seed_local.sql")).execute(dataSource);
    }

    @Test
    void cenarioDoSeedSemAutorizacoes() {
        semear();

        Resumo r = obterResumo.executar();

        assertThat(r.totalProcedimentos()).isEqualTo(3);
        assertThat(r.totalAutorizacoes()).isZero();
        assertThat(r.faixasCobertas()).isEqualTo(3);
        assertThat(r.coberturaPorFaixa()).containsExactly(new CoberturaFaixa("CRIANCA", 2), new CoberturaFaixa("ADOLESCENTE", 2),
                new CoberturaFaixa("ADULTO", 1));
        assertThat(r.distribuicaoSexos().masculino()).isEqualTo(2);
        assertThat(r.distribuicaoSexos().feminino()).isEqualTo(3);
        assertThat(r.ultimasAutorizacoes()).isEmpty();
    }

    @Test
    void bancoVazioDevolveZerosEListasVazias() {
        Resumo r = obterResumo.executar();

        assertThat(r.totalProcedimentos()).isZero();
        assertThat(r.totalAutorizacoes()).isZero();
        assertThat(r.faixasCobertas()).isZero();
        assertThat(r.coberturaPorFaixa()).isEmpty();
        assertThat(r.distribuicaoSexos().masculino()).isZero();
        assertThat(r.distribuicaoSexos().feminino()).isZero();
        assertThat(r.ultimasAutorizacoes()).isEmpty();
    }

    @Test
    void procedimentoComMEFContaNosDoisSexos() {
        jdbc.update("insert into procedimento (numero_procedimento) values (9001)");
        jdbc.update("insert into procedimento_sexo (procedimento_id, sexo) select id, s from procedimento, "
                + "unnest(array['M','F']) as s");
        jdbc.update("insert into procedimento_faixa_etaria (procedimento_id, faixa) select id, 'ADULTO' from procedimento");

        Resumo r = obterResumo.executar();

        assertThat(r.distribuicaoSexos().masculino()).isEqualTo(1);
        assertThat(r.distribuicaoSexos().feminino()).isEqualTo(1);
        assertThat(r.faixasCobertas()).isEqualTo(1);
    }

    @Test
    void ultimasAutorizacoesLimitadasAQuatroMaisRecentesPrimeiroComDataNulaPorUltimo() {
        semear();
        for (int i = 1; i <= 5; i++) {
            jdbc.update("insert into autorizacao (procedimento_id, permitido, criada_em) "
                    + "select id, true, now() + (? * interval '1 minute') from procedimento "
                    + "where numero_procedimento = 4567", i);
        }
        jdbc.update("insert into autorizacao (procedimento_id, permitido, criada_em) "
                + "select id, true, null from procedimento where numero_procedimento = 1234");

        Resumo r = obterResumo.executar();

        assertThat(r.totalAutorizacoes()).isEqualTo(6);
        assertThat(r.ultimasAutorizacoes()).hasSize(4);
        assertThat(r.ultimasAutorizacoes()).allSatisfy(i -> assertThat(i.criadaEm()).isNotNull());
        assertThat(r.ultimasAutorizacoes()).extracting(i -> i.criadaEm())
                .isSortedAccordingTo(java.util.Comparator.reverseOrder());
    }
}
