package org.maia.autorizador.shared.error;

/** Limite de tentativas excedido; carrega o tempo de espera para o cabeçalho {@code Retry-After}. */
public class LimiteExcedidoException extends BusinessRuleException {

    private final long retryAfterSegundos;

    public LimiteExcedidoException(String code, String message, long retryAfterSegundos) {
        super(code, Categoria.LIMITE_EXCEDIDO, message);
        this.retryAfterSegundos = retryAfterSegundos;
    }

    public long getRetryAfterSegundos() {
        return retryAfterSegundos;
    }
}
