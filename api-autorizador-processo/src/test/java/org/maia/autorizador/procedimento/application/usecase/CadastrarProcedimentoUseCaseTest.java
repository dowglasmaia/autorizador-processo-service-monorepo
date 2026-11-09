package org.maia.autorizador.procedimento.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.maia.autorizador.procedimento.domain.exception.ProcedimentoDuplicadoException;
import org.maia.autorizador.procedimento.application.usecase.ObterProcedimentoUseCase.Catalogo;

class CadastrarProcedimentoUseCaseTest {

    private final ProcedimentoRepositoryFake repositorio = new ProcedimentoRepositoryFake();
    private final CadastrarProcedimentoUseCase useCase = new CadastrarProcedimentoUseCase(repositorio);

    @Test
    void cadastraEDevolveComIdEData() {
        var criado = useCase.executar(9999, List.of("f", "M"), List.of("IDOSO", "CRIANCA"));

        assertThat(criado.id()).isNotNull();
        assertThat(criado.criadoEm()).isNotNull();
        assertThat(criado.sexos()).containsExactly("M", "F");
        assertThat(criado.faixasEtarias()).containsExactly("CRIANCA", "IDOSO");
        assertThat(repositorio.buscarPorNumero(9999)).isPresent();
    }

    @Test
    void aceitaListasVazias() {
        var criado = useCase.executar(1, List.of(), List.of());

        assertThat(criado.sexos()).isEmpty();
        assertThat(criado.faixasEtarias()).isEmpty();
    }

    @Test
    void numeroRepetidoLancaDuplicado() {
        useCase.executar(1234, List.of("M"), List.of("CRIANCA"));

        assertThatThrownBy(() -> useCase.executar(1234, List.of("F"), List.of("ADULTO")))
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
    void semDadosDeCatalogoAplicaPadroes() {
        var criado = useCase.executar(20, List.of(), List.of(), new Catalogo("Eco", null,
                "EXAMES_IMAGEM", null, null, null, null, null, null));

        assertThat(criado.catalogo().nome()).isEqualTo("Eco");
        assertThat(criado.catalogo().tipoCobertura()).isEqualTo("PLANO");
        assertThat(criado.catalogo().exigeAutorizacaoPrevia()).isFalse();
        assertThat(criado.catalogo().exigeJustificativaMedica()).isFalse();
        assertThat(criado.catalogo().quantidadeMaxima()).isNull();
        assertThat(criado.catalogo().periodo()).isNull();
    }

    @Test
    void cadastroSemCatalogoTambemRecebePadroes() {
        var criado = useCase.executar(21, List.of("M"), List.of("CRIANCA"));

        assertThat(criado.catalogo().nome()).isNull();
        assertThat(criado.catalogo().tipoCobertura()).isEqualTo("PLANO");
        assertThat(criado.catalogo().exigeAutorizacaoPrevia()).isFalse();
    }

    @Test
    void persisteEDevolveOsDadosInformados() {
        var dados = new Catalogo("  Cirurgia  ", "40101010", "CIRURGIAS", "ORTOPEDIA", "REGRAS", 2, "ANO", true,
                true);

        var criado = useCase.executar(22, List.of(), List.of(), dados);

        assertThat(criado.catalogo()).isEqualTo(new Catalogo("Cirurgia", "40101010", "CIRURGIAS", "ORTOPEDIA",
                "REGRAS", 2, "ANO", true, true));
        assertThat(repositorio.buscarPorId(criado.id()).orElseThrow().catalogo().nome()).isEqualTo("Cirurgia");
    }

    @Test
    void parQuantidadePeriodoInvalidoNaoGrava() {
        var semPeriodo = new Catalogo("x", null, "CONSULTAS", null, null, 2, null, null, null);

        assertThatThrownBy(() -> useCase.executar(23, List.of(), List.of(), semPeriodo))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(repositorio.buscarPorNumero(23)).isEmpty();
    }

    @Test
    void entradaInvalidaNaoGrava() {
        assertThatThrownBy(() -> useCase.executar(1, List.of("X"), List.of())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> useCase.executar(0, List.of(), List.of())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> useCase.executar(1, List.of(), List.of("BEBE")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(repositorio.listar(null, true, 0, 10).totalItens()).isZero();
    }
}
