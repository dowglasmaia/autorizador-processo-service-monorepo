package org.maia.autorizador.autenticacao.domain.model;

/** Token de acesso emitido e sua validade em segundos. */
public record TokenEmitido(String valor, long expiraEmSegundos) {
}
