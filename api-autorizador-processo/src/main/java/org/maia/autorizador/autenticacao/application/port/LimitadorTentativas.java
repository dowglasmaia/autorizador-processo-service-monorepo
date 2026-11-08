package org.maia.autorizador.autenticacao.application.port;

import java.time.Duration;
import java.util.Optional;

/** Porta de saída: limite de falhas de login por chave (e-mail + IP). */
public interface LimitadorTentativas {

    /** Tempo restante de bloqueio da chave, se o limite de falhas já foi atingido. */
    Optional<Duration> bloqueioRestante(String chave);

    void registrarFalha(String chave);

    void limpar(String chave);
}
