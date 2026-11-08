package org.maia.autorizador.autorizacao.application.usecase;

import java.time.Instant;
import org.maia.autorizador.autorizacao.application.port.AutorizacaoRepository;
import org.maia.autorizador.autorizacao.domain.exception.AutorizacaoNegadaException;
import org.maia.autorizador.autorizacao.domain.model.Autorizacao;
import org.maia.autorizador.procedimento.application.usecase.ObterProcedimentoUseCase;
import org.maia.autorizador.procedimento.application.usecase.ObterProcedimentoUseCase.Resultado;
import org.maia.autorizador.procedimento.domain.model.Procedimento;
import org.maia.autorizador.procedimento.domain.model.Sexo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Solicita uma autorização (RN5–RN8): carrega o procedimento pelo número, avalia idade e sexo e, só se autorizado,
 * grava. A negativa lança {@link AutorizacaoNegadaException} sem persistir nada. Idade e sexo não são gravados nem
 * logados.
 */
@Service
@Transactional
public class SolicitarAutorizacaoUseCase {

    private static final Logger log = LoggerFactory.getLogger(SolicitarAutorizacaoUseCase.class);

    /** Autorização concedida, com o procedimento no formato das bordas. */
    public record Concedida(long id, Resultado procedimento, boolean permitido, Instant criadaEm) {
    }

    private final ObterProcedimentoUseCase procedimentos;
    private final AutorizacaoRepository autorizacoes;

    public SolicitarAutorizacaoUseCase(ObterProcedimentoUseCase procedimentos, AutorizacaoRepository autorizacoes) {
        this.procedimentos = procedimentos;
        this.autorizacoes = autorizacoes;
    }

    /**
     * @param sexo {@code M} ou {@code F}, sem diferença de caixa; outro valor lança {@link IllegalArgumentException}
     * @throws org.maia.autorizador.procedimento.domain.exception.ProcedimentoNaoEncontradoException se o número não existe
     * @throws AutorizacaoNegadaException se as regras do procedimento negam a solicitação
     */
    public Concedida executar(int numProcedimento, int idade, String sexo) {
        Sexo sexoNormalizado = Sexo.parse(sexo);
        Procedimento procedimento = procedimentos.porNumero(numProcedimento);
        if (!procedimento.avaliar(idade, sexoNormalizado)) {
            log.info("Autorização negada: procedimento={}", numProcedimento);
            throw AutorizacaoNegadaException.para(numProcedimento);
        }
        Autorizacao salva = autorizacoes.salvar(Autorizacao.conceder(procedimento));
        log.info("Autorização concedida: id={} procedimento={}", salva.id(), numProcedimento);
        return new Concedida(salva.id(), Resultado.de(procedimento), salva.permitido(), salva.criadaEm());
    }
}
