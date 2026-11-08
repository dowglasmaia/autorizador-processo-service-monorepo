package org.maia.autorizador.procedimento.domain.model;

/**
 * Dados informativos de catálogo do procedimento (NEW-01). Não participam de {@link Procedimento#avaliar}.
 * Todos os campos podem ser nulos em procedimentos anteriores ao catálogo estendido ({@link #VAZIO}).
 */
public record DadosCatalogo(String nome, String codigoTuss, Categoria categoria, Especialidade especialidade,
        TipoCobertura tipoCobertura, Integer quantidadeMaxima, Periodo periodo, Boolean exigeAutorizacaoPrevia,
        Boolean exigeJustificativaMedica) {

    public static final int NOME_MAXIMO = 200;
    public static final int CODIGO_TUSS_MAXIMO = 30;

    public static final DadosCatalogo VAZIO = new DadosCatalogo(null, null, null, null, null, null, null, null, null);

    /** Aplica as invariantes de criação: nome 1..200 após trim, código TUSS ≤ 30, quantidade ≥ 1 com período. */
    public static DadosCatalogo validado(String nome, String codigoTuss, Categoria categoria,
            Especialidade especialidade, TipoCobertura tipoCobertura, Integer quantidadeMaxima, Periodo periodo,
            Boolean exigeAutorizacaoPrevia, Boolean exigeJustificativaMedica) {
        String nomeAparado = nome == null ? null : nome.strip();
        if (nomeAparado != null && (nomeAparado.isEmpty() || nomeAparado.length() > NOME_MAXIMO)) {
            throw new IllegalArgumentException("O nome deve ter de 1 a " + NOME_MAXIMO + " caracteres.");
        }
        if (codigoTuss != null && codigoTuss.length() > CODIGO_TUSS_MAXIMO) {
            throw new IllegalArgumentException("O código TUSS deve ter no máximo " + CODIGO_TUSS_MAXIMO + " caracteres.");
        }
        if (quantidadeMaxima != null && quantidadeMaxima < 1) {
            throw new IllegalArgumentException("A quantidade máxima deve ser maior ou igual a 1.");
        }
        if ((quantidadeMaxima == null) != (periodo == null)) {
            throw new IllegalArgumentException("A quantidade máxima e o período devem ser informados juntos.");
        }
        return new DadosCatalogo(nomeAparado, codigoTuss, categoria, especialidade, tipoCobertura, quantidadeMaxima,
                periodo, exigeAutorizacaoPrevia, exigeJustificativaMedica);
    }
}
