package org.maia.autorizador.procedimento.domain.exception;

import org.maia.autorizador.shared.error.BusinessRuleException;

/** Procedimento inexistente (404 {@code PROCEDIMENTO_NAO_ENCONTRADO}). */
public class ProcedimentoNaoEncontradoException extends BusinessRuleException {

    public static final String CODE = "PROCEDIMENTO_NAO_ENCONTRADO";

    public ProcedimentoNaoEncontradoException(String message) {
        super(CODE, Categoria.NAO_ENCONTRADO, message);
    }

    public static ProcedimentoNaoEncontradoException porId(long id) {
        return new ProcedimentoNaoEncontradoException("Procedimento " + id + " não encontrado.");
    }

    public static ProcedimentoNaoEncontradoException porNumero(int numProcedimento) {
        return new ProcedimentoNaoEncontradoException("Procedimento de número " + numProcedimento + " não encontrado.");
    }
}
