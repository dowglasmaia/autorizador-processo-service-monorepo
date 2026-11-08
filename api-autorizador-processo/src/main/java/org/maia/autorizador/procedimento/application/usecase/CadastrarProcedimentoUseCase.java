package org.maia.autorizador.procedimento.application.usecase;

import java.util.Collection;
import org.maia.autorizador.procedimento.application.usecase.ObterProcedimentoUseCase.Resultado;
import org.maia.autorizador.procedimento.application.port.ProcedimentoRepository;
import org.maia.autorizador.procedimento.domain.exception.ProcedimentoDuplicadoException;
import org.maia.autorizador.procedimento.domain.model.Procedimento;
import org.maia.autorizador.procedimento.domain.model.Sexo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cadastra um procedimento. A checagem prévia dá a resposta rápida; a garantia contra a corrida é o UNIQUE do banco,
 * cuja violação o adaptador da porta traduz em {@link ProcedimentoDuplicadoException}.
 */
@Service
@Transactional
public class CadastrarProcedimentoUseCase {

    private final ProcedimentoRepository procedimentos;

    public CadastrarProcedimentoUseCase(ProcedimentoRepository procedimentos) {
        this.procedimentos = procedimentos;
    }

    /** @param sexos {@code M} ou {@code F}, sem diferença de caixa; valor inválido lança {@link IllegalArgumentException} */
    public Resultado executar(int numProcedimento, Collection<String> sexos, Collection<Integer> idades) {
        Procedimento novo = Procedimento.create(numProcedimento, sexos.stream().map(Sexo::parse).toList(), idades);
        if (procedimentos.buscarPorNumero(numProcedimento).isPresent()) {
            throw new ProcedimentoDuplicadoException(numProcedimento);
        }
        return Resultado.de(procedimentos.salvar(novo));
    }
}
