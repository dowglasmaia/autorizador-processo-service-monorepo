package org.maia.autorizador.autorizacao.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.maia.autorizador.autorizacao.application.port.AutorizacaoRepository;
import org.maia.autorizador.autorizacao.application.usecase.ListarAutorizacoesUseCase;
import org.maia.autorizador.autorizacao.application.usecase.SolicitarAutorizacaoUseCase;
import org.maia.autorizador.autorizacao.domain.exception.AutorizacaoNegadaException;
import org.maia.autorizador.autorizacao.domain.model.Autorizacao;
import org.maia.autorizador.autorizacao.domain.model.Pagina;
import org.maia.autorizador.procedimento.application.port.ProcedimentoRepository;
import org.maia.autorizador.procedimento.application.usecase.ObterProcedimentoUseCase;
import org.maia.autorizador.procedimento.domain.exception.ProcedimentoNaoEncontradoException;
import org.maia.autorizador.procedimento.domain.model.Procedimento;
import org.maia.autorizador.procedimento.domain.model.Sexo;

/** Casos de uso de autorização com repositórios em memória (sem Spring nem banco). */
class AutorizacaoUseCasesTest {

    private final ProcedimentosFake procedimentos = new ProcedimentosFake();
    private final AutorizacoesFake autorizacoes = new AutorizacoesFake();
    private final ObterProcedimentoUseCase obter = new ObterProcedimentoUseCase(procedimentos);
    private final SolicitarAutorizacaoUseCase solicitar = new SolicitarAutorizacaoUseCase(obter, autorizacoes);
    private final ListarAutorizacoesUseCase listar = new ListarAutorizacoesUseCase(autorizacoes, obter);

    AutorizacaoUseCasesTest() {
        procedimentos.adicionar(1, 4567, List.of(Sexo.M, Sexo.F), List.of(10, 20, 30));
        procedimentos.adicionar(2, 1234, List.of(Sexo.F), List.of(10, 20));
        procedimentos.adicionar(3, 7, List.of(), List.of());
    }

    @Test
    void autorizadoGravaEDevolveComProcedimento() {
        var concedida = solicitar.executar(4567, 20, "F");

        assertThat(concedida.permitido()).isTrue();
        assertThat(concedida.id()).isPositive();
        assertThat(concedida.criadaEm()).isNotNull();
        assertThat(concedida.procedimento().numProcedimento()).isEqualTo(4567);
        assertThat(autorizacoes.itens).hasSize(1);
    }

    @Test
    void negadoPorSexoNaoGrava() {
        assertThatThrownBy(() -> solicitar.executar(1234, 20, "M")).isInstanceOf(AutorizacaoNegadaException.class);
        assertThat(autorizacoes.itens).isEmpty();
    }

    @Test
    void negadoPorIdadeNaoGrava() {
        assertThatThrownBy(() -> solicitar.executar(1234, 99, "F")).isInstanceOf(AutorizacaoNegadaException.class)
                .hasMessageNotContaining("99");
        assertThat(autorizacoes.itens).isEmpty();
    }

    @Test
    void procedimentoSemSexosNemIdadesNegaSempre() {
        assertThatThrownBy(() -> solicitar.executar(7, 0, "M")).isInstanceOf(AutorizacaoNegadaException.class);
        assertThatThrownBy(() -> solicitar.executar(7, 30, "F")).isInstanceOf(AutorizacaoNegadaException.class);
        assertThat(autorizacoes.itens).isEmpty();
    }

    @Test
    void procedimentoInexistenteLanca404() {
        assertThatThrownBy(() -> solicitar.executar(55555, 30, "M"))
                .isInstanceOf(ProcedimentoNaoEncontradoException.class);
        assertThat(autorizacoes.itens).isEmpty();
    }

    @Test
    void sexoMinusculoEAceito() {
        assertThat(solicitar.executar(4567, 10, "m").permitido()).isTrue();
        assertThat(solicitar.executar(1234, 10, "f").permitido()).isTrue();
    }

    @Test
    void sexoPorExtensoEInvalido() {
        assertThatThrownBy(() -> solicitar.executar(4567, 20, "Masculino"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(autorizacoes.itens).isEmpty();
    }

    @Test
    void duasAutorizacoesDoMesmoProcedimentoCoexistem() {
        solicitar.executar(4567, 20, "F");
        solicitar.executar(4567, 20, "F");

        assertThat(autorizacoes.itens).hasSize(2);
    }

    @Test
    void listaPaginadaMaisRecentePrimeiroComProcedimentoEmLote() {
        solicitar.executar(4567, 20, "F");
        solicitar.executar(1234, 10, "F");
        solicitar.executar(4567, 30, "M");

        var pagina = listar.executar(0, 2);

        assertThat(pagina.itens()).extracting(i -> i.procedimento().numProcedimento()).containsExactly(4567, 1234);
        assertThat(pagina.itens()).extracting(i -> i.id()).containsExactly(3L, 2L);
        assertThat(pagina.totalItens()).isEqualTo(3);
        assertThat(pagina.totalPaginas()).isEqualTo(2);
        assertThat(procedimentos.consultasEmLote).isEqualTo(1);
        assertThat(listar.executar(1, 2).itens()).hasSize(1);
        assertThat(listar.executar(5, 2).itens()).isEmpty();
    }

    @Test
    void listaValidaPaginaETamanho() {
        assertThatThrownBy(() -> listar.executar(-1, 10)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> listar.executar(0, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> listar.executar(0, 201)).isInstanceOf(IllegalArgumentException.class);
    }

    private static final class ProcedimentosFake implements ProcedimentoRepository {
        private final List<Procedimento> itens = new ArrayList<>();
        int consultasEmLote;

        void adicionar(long id, int numero, List<Sexo> sexos, List<Integer> idades) {
            itens.add(Procedimento.restore(id, numero, sexos, idades, Instant.now()));
        }

        @Override
        public Procedimento salvar(Procedimento procedimento) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Procedimento> buscarPorId(long id) {
            return itens.stream().filter(p -> p.id() == id).findFirst();
        }

        @Override
        public Optional<Procedimento> buscarPorNumero(int numProcedimento) {
            return itens.stream().filter(p -> p.numProcedimento() == numProcedimento).findFirst();
        }

        @Override
        public List<Procedimento> buscarPorIds(Collection<Long> ids) {
            consultasEmLote++;
            return itens.stream().filter(p -> ids.contains(p.id())).toList();
        }

        @Override
        public org.maia.autorizador.procedimento.domain.model.Pagina<Procedimento> listar(String prefixoNumero,
                boolean crescente, int pagina, int tamanho) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class AutorizacoesFake implements AutorizacaoRepository {
        final List<Autorizacao> itens = new ArrayList<>();

        @Override
        public Autorizacao salvar(Autorizacao autorizacao) {
            Autorizacao salva = Autorizacao.restore(itens.size() + 1L, autorizacao.procedimentoId(),
                    autorizacao.permitido(), Instant.now());
            itens.add(salva);
            return salva;
        }

        @Override
        public Pagina<Autorizacao> listar(int pagina, int tamanho) {
            List<Autorizacao> ordenadas = itens.stream().sorted(Comparator.comparingLong(Autorizacao::id).reversed())
                    .toList();
            int de = Math.min(pagina * tamanho, ordenadas.size());
            int ate = Math.min(de + tamanho, ordenadas.size());
            return new Pagina<>(ordenadas.subList(de, ate), pagina, tamanho, ordenadas.size());
        }
    }
}
