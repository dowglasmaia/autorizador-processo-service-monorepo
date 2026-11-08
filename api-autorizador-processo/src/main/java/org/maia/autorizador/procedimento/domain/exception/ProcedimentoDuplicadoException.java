package org.maia.autorizador.procedimento.domain.exception;

import org.maia.autorizador.shared.error.BusinessRuleException;

/** Já existe procedimento com o número informado (403 {@code PROCEDIMENTO_DUPLICADO}). */
public class ProcedimentoDuplicadoException extends BusinessRuleException {

    public static final String CODE = "PROCEDIMENTO_DUPLICADO";

    public ProcedimentoDuplicadoException(int numProcedimento) {
        super(CODE, Categoria.OPERACAO_NAO_PERMITIDA, "Já existe um procedimento com o número " + numProcedimento + ".");
    }
}
