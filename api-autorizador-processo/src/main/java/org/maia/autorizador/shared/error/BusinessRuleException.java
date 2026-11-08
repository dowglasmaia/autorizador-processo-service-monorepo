package org.maia.autorizador.shared.error;

/**
 * Base das exceções de regra de negócio. Não depende de Spring: o mapeamento para HTTP fica no
 * {@code GlobalExceptionHandler}, a partir da {@link Categoria}.
 */
public class BusinessRuleException extends RuntimeException {

    /** Categoria de status da falha, traduzida para HTTP apenas na borda da API. */
    public enum Categoria {
        /** Operação não permitida (403). */
        OPERACAO_NAO_PERMITIDA,
        /** Recurso inexistente (404). */
        NAO_ENCONTRADO
    }

    private final String code;
    private final Categoria categoria;

    public BusinessRuleException(String code, Categoria categoria, String message) {
        super(message);
        this.code = code;
        this.categoria = categoria;
    }

    public String getCode() {
        return code;
    }

    public Categoria getCategoria() {
        return categoria;
    }
}
