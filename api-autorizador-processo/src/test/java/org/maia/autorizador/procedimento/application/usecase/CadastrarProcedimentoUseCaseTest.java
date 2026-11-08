package org.maia.autorizador.procedimento.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.maia.autorizador.procedimento.domain.exception.ProcedimentoDuplicadoException;

class CadastrarProcedimentoUseCaseTest {

    private final ProcedimentoRepositoryFake repositorio = new ProcedimentoRepositoryFake();
    private final CadastrarProcedimentoUseCase useCase = new CadastrarProcedimentoUseCase(repositorio);

    @Test
    void cadastraEDevolveComIdEData() {
        var criado = useCase.executar(9999, List.of("f", "M"), List.of(30, 18));

        assertThat(criado.id()).isNotNull();
        assertThat(criado.criadoEm()).isNotNull();
        assertThat(criado.sexos()).containsExactly("M", "F");
        assertThat(criado.idades()).containsExactly(18, 30);
        assertThat(repositorio.buscarPorNumero(9999)).isPresent();
    }

    @Test
    void aceitaListasVazias() {
        var criado = useCase.executar(1, List.of(), List.of());

        assertThat(criado.sexos()).isEmpty();
        assertThat(criado.idades()).isEmpty();
    }

    @Test
    void numeroRepetidoLancaDuplicado() {
        useCase.executar(1234, List.of("M"), List.of(10));

        assertThatThrownBy(() -> useCase.executar(1234, List.of("F"), List.of(20)))
                .isInstanceOf(ProcedimentoDuplicadoException.class)
                .extracting("code").isEqualTo("PROCEDIMENTO_DUPLICADO");
    }

    @Test
    void violacaoDoUniqueNaCorridaTambemViraDuplicado() {
        useCase.executar(5, List.of(), List.of());
        repositorio.ocultarNaBusca = true;

        assertThatThrownBy(() -> useCase.executar(5, List.of(), List.of()))
                .isInstanceOf(ProcedimentoDuplicadoException.class);
    }

    @Test
    void entradaInvalidaNaoGrava() {
        assertThatThrownBy(() -> useCase.executar(1, List.of("X"), List.of())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> useCase.executar(0, List.of(), List.of())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> useCase.executar(1, List.of(), List.of(151)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(repositorio.listar(null, true, 0, 10).totalItens()).isZero();
    }
}
