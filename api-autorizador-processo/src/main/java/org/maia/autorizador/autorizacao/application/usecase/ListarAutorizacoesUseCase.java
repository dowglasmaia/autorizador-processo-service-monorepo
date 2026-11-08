package org.maia.autorizador.autorizacao.application.usecase;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.maia.autorizador.autorizacao.application.port.AutorizacaoRepository;
import org.maia.autorizador.autorizacao.domain.model.Autorizacao;
import org.maia.autorizador.autorizacao.domain.model.Pagina;
import org.maia.autorizador.procedimento.application.usecase.ObterProcedimentoUseCase;
import org.maia.autorizador.procedimento.application.usecase.ObterProcedimentoUseCase.Resultado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lista o histórico de autorizações, das mais recentes para as mais antigas, com os procedimentos em lote. */
@Service
@Transactional(readOnly = true)
public class ListarAutorizacoesUseCase {

    public static final int TAMANHO_MAXIMO = 200;

    /** Autorização no formato das bordas. */
    public record Item(long id, Resultado procedimento, boolean permitido, Instant criadaEm) {
    }

    public record PaginaResultado(List<Item> itens, int pagina, int tamanho, long totalItens, int totalPaginas) {
    }

    private final AutorizacaoRepository autorizacoes;
    private final ObterProcedimentoUseCase procedimentos;

    public ListarAutorizacoesUseCase(AutorizacaoRepository autorizacoes, ObterProcedimentoUseCase procedimentos) {
        this.autorizacoes = autorizacoes;
        this.procedimentos = procedimentos;
    }

    /**
     * @param pagina começa em 0
     * @param tamanho de 1 a {@value #TAMANHO_MAXIMO}
     */
    public PaginaResultado executar(int pagina, int tamanho) {
        if (pagina < 0) {
            throw new IllegalArgumentException("A página deve ser maior ou igual a 0.");
        }
        if (tamanho < 1 || tamanho > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException("O tamanho deve estar entre 1 e " + TAMANHO_MAXIMO + ".");
        }
        Pagina<Autorizacao> resultado = autorizacoes.listar(pagina, tamanho);
        Map<Long, Resultado> porId = procedimentos.porIds(
                resultado.itens().stream().map(Autorizacao::procedimentoId).distinct().toList());
        List<Item> itens = resultado.itens().stream()
                .map(a -> new Item(a.id(), porId.get(a.procedimentoId()), a.permitido(), a.criadaEm()))
                .toList();
        int totalPaginas = (int) ((resultado.total() + tamanho - 1) / tamanho);
        return new PaginaResultado(itens, resultado.pagina(), resultado.tamanho(), resultado.total(), totalPaginas);
    }
}
