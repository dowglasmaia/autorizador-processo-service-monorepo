package org.maia.autorizador.autorizacao.domain.model;

import java.util.List;

/** Página de resultados (o índice da página começa em zero). */
public record Pagina<T>(List<T> itens, int pagina, int tamanho, long total) {
}
