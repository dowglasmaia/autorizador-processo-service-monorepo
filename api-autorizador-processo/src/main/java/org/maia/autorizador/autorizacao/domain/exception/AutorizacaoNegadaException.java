package org.maia.autorizador.autorizacao.domain.exception;

import org.maia.autorizador.shared.error.BusinessRuleException;

/** Solicitação negada pelas regras do procedimento (403 {@code AUTORIZACAO_NEGADA}); nada é gravado (RN8). */
public class AutorizacaoNegadaException extends BusinessRuleException {

    public static final String CODE = "AUTORIZACAO_NEGADA";

    public AutorizacaoNegadaException(String message) {
        super(CODE, Categoria.OPERACAO_NAO_PERMITIDA, message);
    }

    /** A mensagem não revela idade nem sexo do paciente. */
    public static AutorizacaoNegadaException para(int numProcedimento) {
        return new AutorizacaoNegadaException("Autorização negada para o procedimento " + numProcedimento + ".");
    }
}
