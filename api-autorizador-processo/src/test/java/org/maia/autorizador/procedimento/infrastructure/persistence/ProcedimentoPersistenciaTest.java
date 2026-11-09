package org.maia.autorizador.procedimento.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.maia.autorizador.procedimento.application.port.ProcedimentoRepository;
import org.maia.autorizador.procedimento.domain.exception.ProcedimentoDuplicadoException;
import org.maia.autorizador.procedimento.domain.model.Procedimento;
import org.maia.autorizador.procedimento.domain.model.FaixaEtaria;
import org.maia.autorizador.procedimento.domain.model.Sexo;
import org.maia.autorizador.support.IntegrationTestBase;
import org.springframework.beans.factory.annotation.Autowired;

/** Persistência de procedimentos em PostgreSQL 16 real (Testcontainers), contra as migrações V1 e V2. */
class ProcedimentoPersistenciaTest extends IntegrationTestBase {

    @Autowired
    ProcedimentoRepository procedimentos;

    @Test
    void idaEVoltaPreservaNumeroSexosIdadesEData() {
        Procedimento salvo = procedimentos.salvar(Procedimento.create(9999, List.of(Sexo.F, Sexo.M), List.of(FaixaEtaria.ADULTO, FaixaEtaria.ADOLESCENTE)));

        assertThat(salvo.id()).isNotNull();
        assertThat(salvo.criadoEm()).isNotNull();
        Procedimento lido = procedimentos.buscarPorId(salvo.id()).orElseThrow();
        assertThat(lido.numProcedimento()).isEqualTo(9999);
        assertThat(lido.sexos()).containsExactly(Sexo.M, Sexo.F);
        assertThat(lido.faixasEtarias()).containsExactly(FaixaEtaria.ADOLESCENTE, FaixaEtaria.ADULTO);
        assertThat(lido.criadoEm()).isEqualTo(salvo.criadoEm());
        assertThat(procedimentos.buscarPorNumero(9999)).get().extracting(Procedimento::id).isEqualTo(salvo.id());
        assertThat(procedimentos.buscarPorNumero(1)).isEmpty();
        assertThat(procedimentos.buscarPorId(12345)).isEmpty();
    }

    @Test
    void buscaEmLotePorIdsDevolveSomenteOsExistentes() {
        Procedimento a = procedimentos.salvar(Procedimento.create(8801, List.of(Sexo.M), List.of(FaixaEtaria.CRIANCA, FaixaEtaria.ADOLESCENTE)));
        Procedimento b = procedimentos.salvar(Procedimento.create(8802, List.of(Sexo.F), List.of(FaixaEtaria.ADULTO)));

        List<Procedimento> lote = procedimentos.buscarPorIds(List.of(a.id(), b.id(), 987654L));

        assertThat(lote).extracting(Procedimento::numProcedimento).containsExactlyInAnyOrder(8801, 8802);
        assertThat(lote).filteredOn(p -> p.numProcedimento() == 8801).singleElement()
                .satisfies(p -> assertThat(p.faixasEtarias()).containsExactly(FaixaEtaria.CRIANCA, FaixaEtaria.ADOLESCENTE));
        assertThat(procedimentos.buscarPorIds(List.of())).isEmpty();
    }

    @Test
    void listasVaziasSaoGravadasEDevolvidasVazias() {
        Procedimento salvo = procedimentos.salvar(Procedimento.create(7, List.of(), List.of()));

        Procedimento lido = procedimentos.buscarPorId(salvo.id()).orElseThrow();
        assertThat(lido.sexos()).isEmpty();
        assertThat(lido.faixasEtarias()).isEmpty();
    }

    @Test
    void numeroRepetidoViraProcedimentoDuplicado() {
        procedimentos.salvar(Procedimento.create(1234, List.of(Sexo.M), List.of(FaixaEtaria.CRIANCA)));

        assertThatThrownBy(() -> procedimentos.salvar(Procedimento.create(1234, List.of(Sexo.F), List.of(FaixaEtaria.ADOLESCENTE))))
                .isInstanceOf(ProcedimentoDuplicadoException.class);
        assertThat(procedimentos.listar(null, true, 0, 10).totalItens()).isEqualTo(1);
    }

    @Test
    void listaOrdenaFiltraPorPrefixoEPagina() {
        for (int numero : new int[] {4567, 1234, 6789, 12}) {
            procedimentos.salvar(Procedimento.create(numero, List.of(Sexo.M), List.of(FaixaEtaria.ADOLESCENTE, FaixaEtaria.CRIANCA)));
        }

        var todos = procedimentos.listar(null, true, 0, 10);
        assertThat(todos.itens()).extracting(Procedimento::numProcedimento).containsExactly(12, 1234, 4567, 6789);
        assertThat(todos.itens()).allSatisfy(p -> assertThat(p.faixasEtarias()).containsExactly(FaixaEtaria.CRIANCA, FaixaEtaria.ADOLESCENTE));
        assertThat(procedimentos.listar(null, false, 0, 2).itens()).extracting(Procedimento::numProcedimento)
                .containsExactly(6789, 4567);
        assertThat(procedimentos.listar("12", true, 0, 10).itens()).extracting(Procedimento::numProcedimento)
                .containsExactly(12, 1234);
        var filtrada = procedimentos.listar("12", true, 0, 1);
        assertThat(filtrada.totalItens()).isEqualTo(2);
        assertThat(filtrada.totalPaginas()).isEqualTo(2);
    }

    @Test
    void bancoRejeitaDuplicidadeMesmoForaDaAplicacao() {
        jdbc.update("insert into procedimento (numero_procedimento) values (55)");

        assertThatThrownBy(() -> jdbc.update("insert into procedimento (numero_procedimento) values (55)"))
                .hasMessageContaining("uq_procedimento_numero");
    }
}
