package org.maia.autorizador.autorizacao.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.maia.autorizador.autorizacao.application.port.AutorizacaoRepository;
import org.maia.autorizador.autorizacao.application.usecase.SolicitarAutorizacaoUseCase;
import org.maia.autorizador.autorizacao.domain.exception.AutorizacaoNegadaException;
import org.maia.autorizador.autorizacao.domain.model.Autorizacao;
import org.maia.autorizador.procedimento.application.port.ProcedimentoRepository;
import org.maia.autorizador.procedimento.domain.model.Procedimento;
import org.maia.autorizador.procedimento.domain.model.Sexo;
import org.maia.autorizador.support.IntegrationTestBase;
import org.springframework.beans.factory.annotation.Autowired;

/** Persistência de autorizações em PostgreSQL 16 real (Testcontainers), contra as migrações V1 e V2. */
class AutorizacaoPersistenciaTest extends IntegrationTestBase {

    @Autowired
    AutorizacaoRepository autorizacoes;

    @Autowired
    ProcedimentoRepository procedimentos;

    @Autowired
    SolicitarAutorizacaoUseCase solicitar;

    private Procedimento procedimento(int numero) {
        return procedimentos.salvar(Procedimento.create(numero, List.of(Sexo.F), List.of(20)));
    }

    @Test
    void idaEVoltaPreservaProcedimentoPermitidoEData() {
        Procedimento proc = procedimento(4567);

        Autorizacao salva = autorizacoes.salvar(Autorizacao.conceder(proc));

        assertThat(salva.id()).isNotNull();
        assertThat(salva.criadaEm()).isNotNull();
        assertThat(salva.permitido()).isTrue();
        var pagina = autorizacoes.listar(0, 10);
        assertThat(pagina.total()).isEqualTo(1);
        assertThat(pagina.itens()).singleElement().satisfies(a -> {
            assertThat(a.id()).isEqualTo(salva.id());
            assertThat(a.procedimentoId()).isEqualTo(proc.id());
            assertThat(a.criadaEm()).isEqualTo(salva.criadaEm());
        });
    }

    @Test
    void duasAutorizacoesDoMesmoProcedimentoCoexistemMaisRecentePrimeiro() {
        Procedimento proc = procedimento(4567);

        Autorizacao primeira = autorizacoes.salvar(Autorizacao.conceder(proc));
        Autorizacao segunda = autorizacoes.salvar(Autorizacao.conceder(proc));

        assertThat(autorizacoes.listar(0, 10).itens()).extracting(Autorizacao::id)
                .containsExactly(segunda.id(), primeira.id());
        assertThat(autorizacoes.listar(1, 1).itens()).extracting(Autorizacao::id).containsExactly(primeira.id());
    }

    @Test
    void negativaNaoGravaNada() {
        procedimento(1234);

        assertThatThrownBy(() -> solicitar.executar(1234, 99, "M")).isInstanceOf(AutorizacaoNegadaException.class);

        assertThat(jdbc.queryForObject("select count(*) from autorizacao", Integer.class)).isZero();
    }

    @Test
    void concessaoGravaPelaCamadaDeCasoDeUso() {
        procedimento(1234);

        var concedida = solicitar.executar(1234, 20, "f");

        assertThat(concedida.id()).isPositive();
        assertThat(jdbc.queryForObject("select count(*) from autorizacao where permitido", Integer.class)).isOne();
    }

    @Test
    void criacaoGeraRevisaoEAuditoria() {
        Autorizacao salva = autorizacoes.salvar(Autorizacao.conceder(procedimento(4567)));

        assertThat(jdbc.queryForObject("""
                select count(*) from autorizacao_audit a join revinfo r on r.rev = a.rev
                where a.id = ? and a.revtype = 0 and a.permitido and a.criada_em is not null
                """, Integer.class, salva.id())).isEqualTo(1);
    }

    @Test
    void bancoRejeitaAutorizacaoDeProcedimentoInexistente() {
        assertThatThrownBy(() -> jdbc.update("insert into autorizacao (procedimento_id, permitido) values (999, true)"))
                .hasMessageContaining("fk_autorizacao_procedimento");
    }
}
