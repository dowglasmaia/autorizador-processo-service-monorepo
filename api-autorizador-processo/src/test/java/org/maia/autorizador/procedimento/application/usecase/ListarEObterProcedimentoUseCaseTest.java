package org.maia.autorizador.procedimento.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.maia.autorizador.procedimento.domain.exception.ProcedimentoNaoEncontradoException;
import org.maia.autorizador.procedimento.domain.model.Procedimento;
import org.maia.autorizador.procedimento.domain.model.Sexo;

class ListarEObterProcedimentoUseCaseTest {

    private final ProcedimentoRepositoryFake repositorio = new ProcedimentoRepositoryFake();
    private final ListarProcedimentosUseCase listar = new ListarProcedimentosUseCase(repositorio);
    private final ObterProcedimentoUseCase obter = new ObterProcedimentoUseCase(repositorio);

    @BeforeEach
    void semear() {
        for (int numero : new int[] {4567, 1234, 6789, 12}) {
            repositorio.salvar(Procedimento.create(numero, List.of(Sexo.M), List.of(20)));
        }
    }

    private List<Integer> numeros(String prefixo, boolean crescente, int pagina, int tamanho) {
        return listar.executar(prefixo, crescente, pagina, tamanho).itens().stream()
                .map(ObterProcedimentoUseCase.Resultado::numProcedimento).toList();
    }

    @Test
    void ordenaPorNumeroNasDuasDirecoes() {
        assertThat(numeros(null, true, 0, 10)).containsExactly(12, 1234, 4567, 6789);
        assertThat(numeros(null, false, 0, 10)).containsExactly(6789, 4567, 1234, 12);
    }

    @Test
    void filtraPorPrefixoEIgnoraFiltroEmBranco() {
        assertThat(numeros("12", true, 0, 10)).containsExactly(12, 1234);
        assertThat(numeros("  ", true, 0, 10)).hasSize(4);
        assertThat(numeros("9", true, 0, 10)).isEmpty();
    }

    @Test
    void paginaEInformaTotais() {
        var segunda = listar.executar(null, true, 1, 3);

        assertThat(segunda.itens()).extracting(ObterProcedimentoUseCase.Resultado::numProcedimento).containsExactly(6789);
        assertThat(segunda.totalItens()).isEqualTo(4);
        assertThat(segunda.totalPaginas()).isEqualTo(2);
    }

    @Test
    void rejeitaParametrosForaDosLimites() {
        assertThatThrownBy(() -> listar.executar(null, true, -1, 10)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> listar.executar(null, true, 0, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> listar.executar(null, true, 0, 201)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> listar.executar("12a", true, 0, 10)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void obtemPorIdEPorNumero() {
        long id = repositorio.buscarPorNumero(1234).orElseThrow().id();

        assertThat(obter.executar(id).numProcedimento()).isEqualTo(1234);
        assertThat(obter.porNumero(4567).numProcedimento()).isEqualTo(4567);
    }

    @Test
    void inexistenteLancaNaoEncontrado() {
        assertThatThrownBy(() -> obter.executar(999))
                .isInstanceOf(ProcedimentoNaoEncontradoException.class)
                .extracting("code").isEqualTo("PROCEDIMENTO_NAO_ENCONTRADO");
        assertThatThrownBy(() -> obter.porNumero(1)).isInstanceOf(ProcedimentoNaoEncontradoException.class);
    }
}
