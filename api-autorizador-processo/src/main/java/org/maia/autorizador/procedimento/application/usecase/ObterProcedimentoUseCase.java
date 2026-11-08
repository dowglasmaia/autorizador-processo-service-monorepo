package org.maia.autorizador.procedimento.application.usecase;

import java.time.Instant;
import java.util.List;
import org.maia.autorizador.procedimento.application.port.ProcedimentoRepository;
import java.time.Instant;
import java.util.List;
import org.maia.autorizador.procedimento.domain.exception.ProcedimentoNaoEncontradoException;
import java.time.Instant;
import java.util.List;
import org.maia.autorizador.procedimento.domain.model.Procedimento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Obtém um procedimento por identificador ou por número (usado também pela autorização). */
@Service
@Transactional(readOnly = true)
public class ObterProcedimentoUseCase {

    /** Visão do procedimento para as bordas: sexos na ordem M, F e idades em ordem crescente. */
    public record Resultado(long id, int numProcedimento, List<String> sexos, List<Integer> idades,
            Instant criadoEm) {

        public static Resultado de(Procedimento p) {
            return new Resultado(p.id(), p.numProcedimento(), p.sexos().stream().map(Enum::name).toList(),
                    List.copyOf(p.idades()), p.criadoEm());
        }
    }

    private final ProcedimentoRepository procedimentos;

    public ObterProcedimentoUseCase(ProcedimentoRepository procedimentos) {
        this.procedimentos = procedimentos;
    }

    public Resultado executar(long id) {
        return procedimentos.buscarPorId(id).map(Resultado::de)
                .orElseThrow(() -> ProcedimentoNaoEncontradoException.porId(id));
    }

    public Procedimento porNumero(int numProcedimento) {
        return procedimentos.buscarPorNumero(numProcedimento)
                .orElseThrow(() -> ProcedimentoNaoEncontradoException.porNumero(numProcedimento));
    }
}
