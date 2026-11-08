package org.maia.autorizador.shared.error;

/**
 * Entrada inválida atribuída a um campo específico do corpo. O {@code GlobalExceptionHandler} a traduz em
 * {@code 400 VALIDACAO_INVALIDA} com {@code errors[]} apontando o campo.
 */
public class CampoInvalidoException extends RuntimeException {

    private final String campo;

    public CampoInvalidoException(String campo, String message) {
        super(message);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
