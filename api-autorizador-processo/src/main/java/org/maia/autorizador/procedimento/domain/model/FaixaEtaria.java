package org.maia.autorizador.procedimento.domain.model;

/**
 * Faixa etária pela idade em anos completos. Os limites são inclusivos e contíguos: "12 anos e 1 dia até 20 anos"
 * é a adolescência (13 a 20 anos completos). A regra de autorização usa a faixa, não a idade exata.
 */
public enum FaixaEtaria {
    CRIANCA(0, 12),
    ADOLESCENTE(13, 20),
    ADULTO(21, 59),
    IDOSO(60, 150);

    public static final int IDADE_MINIMA = 0;
    public static final int IDADE_MAXIMA = 150;

    private final int de;
    private final int ate;

    FaixaEtaria(int de, int ate) {
        this.de = de;
        this.ate = ate;
    }

    /** Menor idade (inclusive) da faixa. */
    public int de() {
        return de;
    }

    /** Maior idade (inclusive) da faixa. */
    public int ate() {
        return ate;
    }

    public boolean contem(int idade) {
        return idade >= de && idade <= ate;
    }

    /** Faixa da idade informada; fora de 0..150 lança {@link IllegalArgumentException}. */
    public static FaixaEtaria de(int idade) {
        for (FaixaEtaria faixa : values()) {
            if (faixa.contem(idade)) {
                return faixa;
            }
        }
        throw new IllegalArgumentException("Idade fora da faixa " + IDADE_MINIMA + ".." + IDADE_MAXIMA + ": " + idade);
    }
}
