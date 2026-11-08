package org.maia.autorizador.autorizacao.application.port;

import org.maia.autorizador.autorizacao.domain.model.Autorizacao;
import org.maia.autorizador.autorizacao.domain.model.Pagina;

/** Porta de saída: persistência de autorizações. */
public interface AutorizacaoRepository {

    /** Grava uma autorização nova e a devolve com identificador e data. */
    Autorizacao salvar(Autorizacao autorizacao);

    /** Página das autorizações da mais recente para a mais antiga (desempate pelo id decrescente). */
    Pagina<Autorizacao> listar(int pagina, int tamanho);
}
