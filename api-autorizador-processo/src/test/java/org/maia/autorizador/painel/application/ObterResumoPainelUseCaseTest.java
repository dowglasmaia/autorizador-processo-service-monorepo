package org.maia.autorizador.painel.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.maia.autorizador.autorizacao.application.usecase.ListarAutorizacoesUseCase.Item;
import org.maia.autorizador.painel.application.port.PainelConsulta;
import org.maia.autorizador.painel.application.usecase.ObterResumoPainelUseCase;
import org.maia.autorizador.painel.application.model.CoberturaIdade;
import org.maia.autorizador.painel.application.model.DistribuicaoSexos;
import org.maia.autorizador.procedimento.application.usecase.ObterProcedimentoUseCase.Resultado;

class ObterResumoPainelUseCaseTest {

    private record Fake(int procedimentos, int autorizacoes, List<CoberturaIdade> cobertura, DistribuicaoSexos sexos,
            List<Item> ultimas) implements PainelConsulta {
        @Override public int totalProcedimentos() { return procedimentos; }
        @Override public int totalAutorizacoes() { return autorizacoes; }
        @Override public List<CoberturaIdade> coberturaPorIdade() { return cobertura; }
        @Override public DistribuicaoSexos distribuicaoSexos() { return sexos; }
        @Override public List<Item> ultimasAutorizacoes(int limite) { return ultimas; }
    }

    private static Item item(long id) {
        var proc = new Resultado(1, 100, List.of("M"), List.of(10), Instant.EPOCH);
        return new Item(id, proc, true, Instant.EPOCH);
    }

    @Test
    void valoresDoSeed() {
        var fake = new Fake(3, 0, List.of(new CoberturaIdade(10, 2), new CoberturaIdade(20, 2),
                new CoberturaIdade(30, 1)), new DistribuicaoSexos(2, 3), List.of());
        var resumo = new ObterResumoPainelUseCase(fake).executar();

        assertThat(resumo.totalProcedimentos()).isEqualTo(3);
        assertThat(resumo.totalAutorizacoes()).isZero();
        assertThat(resumo.idadesCobertas()).isEqualTo(3);
        assertThat(resumo.coberturaPorIdade()).hasSize(3);
        assertThat(resumo.distribuicaoSexos()).isEqualTo(new DistribuicaoSexos(2, 3));
        assertThat(resumo.ultimasAutorizacoes()).isEmpty();
    }

    @Test
    void vazio() {
        var resumo = new ObterResumoPainelUseCase(
                new Fake(0, 0, List.of(), new DistribuicaoSexos(0, 0), List.of())).executar();

        assertThat(resumo.totalProcedimentos()).isZero();
        assertThat(resumo.idadesCobertas()).isZero();
        assertThat(resumo.coberturaPorIdade()).isEmpty();
        assertThat(resumo.ultimasAutorizacoes()).isEmpty();
    }

    @Test
    void limitaAQuatroUltimasAutorizacoes() {
        List<Item> seis = IntStream.rangeClosed(1, 6).mapToObj(i -> item(7 - i)).toList();
        var resumo = new ObterResumoPainelUseCase(
                new Fake(1, 6, List.of(), new DistribuicaoSexos(1, 0), seis)).executar();

        assertThat(resumo.ultimasAutorizacoes()).extracting(Item::id).containsExactly(6L, 5L, 4L, 3L);
    }
}
