package org.maia.autorizador.procedimento.domain.model;

import java.util.Locale;

/** Sexo aceito por um procedimento. A ordem de declaração (M, F) é a ordem de saída. */
public enum Sexo {
    M,
    F;

    /** Converte o texto sem diferença de caixa e ignorando espaços nas pontas; qualquer outro valor é inválido. */
    public static Sexo parse(String valor) {
        if (valor == null) {
            throw new IllegalArgumentException("Sexo obrigatório.");
        }
        return switch (valor.strip().toUpperCase(Locale.ROOT)) {
            case "M" -> M;
            case "F" -> F;
            default -> throw new IllegalArgumentException("Sexo inválido: use M ou F.");
        };
    }
}
