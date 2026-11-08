package org.maia.autorizador.procedimento.application.usecase;

import java.util.List;
import org.maia.autorizador.procedimento.application.port.ProcedimentoRepository;
import java.util.List;
import org.maia.autorizador.procedimento.domain.model.Pagina;
import java.util.List;
import org.maia.autorizador.procedimento.domain.model.Procedimento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lista procedimentos paginados, ordenados por número, com filtro opcional por prefixo do número. */
@Service
@Transactional(readOnly = true)
public class ListarProcedimentosUseCase {

    public static final int TAMANHO_MAXIMO = 200;

    public record PaginaResultado(List<ObterProcedimentoUseCase.Resultado> itens, int pagina, int tamanho,
            long totalItens, int totalPaginas) {
    }

    private final ProcedimentoRepository procedimentos;

    public ListarProcedimentosUseCase(ProcedimentoRepository procedimentos) {
        this.procedimentos = procedimentos;
    }

    /**
     * @param prefixo prefixo do número (somente dígitos); nulo ou em branco significa sem filtro
     * @param crescente ordenação por número
     * @param pagina começa em 0
     * @param tamanho de 1 a {@value #TAMANHO_MAXIMO}
     */
    public PaginaResultado executar(String prefixo, boolean crescente, int pagina, int tamanho) {
        if (pagina < 0) {
            throw new IllegalArgumentException("A página deve ser maior ou igual a 0.");
        }
        if (tamanho < 1 || tamanho > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException("O tamanho deve estar entre 1 e " + TAMANHO_MAXIMO + ".");
        }
        String filtro = prefixo == null ? null : prefixo.strip();
        if (filtro != null && filtro.isEmpty()) {
            filtro = null;
        }
        if (filtro != null && !filtro.chars().allMatch(c -> c >= '0' && c <= '9')) {
            throw new IllegalArgumentException("O filtro q deve conter somente dígitos.");
        }
        Pagina<Procedimento> resultado = procedimentos.listar(filtro, crescente, pagina, tamanho);
        return new PaginaResultado(resultado.itens().stream().map(ObterProcedimentoUseCase.Resultado::de).toList(),
                resultado.pagina(), resultado.tamanho(), resultado.totalItens(), resultado.totalPaginas());
    }
}
