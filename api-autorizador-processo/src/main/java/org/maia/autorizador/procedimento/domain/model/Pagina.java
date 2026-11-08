package org.maia.autorizador.procedimento.domain.model;

import java.util.List;

/** Página de resultados de uma consulta; {@code pagina} começa em 0. */
public record Pagina<T>(List<T> itens, int pagina, int tamanho, long totalItens) {

    public Pagina {
        itens = List.copyOf(itens);
    }

    public int totalPaginas() {
        return tamanho <= 0 ? 0 : (int) ((totalItens + tamanho - 1) / tamanho);
    }
}
