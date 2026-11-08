package org.maia.autorizador.painel.application.usecase;

import java.util.List;
import org.maia.autorizador.autorizacao.application.usecase.ListarAutorizacoesUseCase.Item;
import org.maia.autorizador.painel.application.port.PainelConsulta;
import org.maia.autorizador.painel.application.model.CoberturaIdade;
import org.maia.autorizador.painel.application.model.DistribuicaoSexos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Monta o resumo do painel (somente leitura) a partir das consultas agregadas. */
@Service
@Transactional(readOnly = true)
public class ObterResumoPainelUseCase {

    public static final int MAXIMO_ULTIMAS = 4;

    public record Resumo(int totalProcedimentos, int totalAutorizacoes, int idadesCobertas,
            List<CoberturaIdade> coberturaPorIdade, DistribuicaoSexos distribuicaoSexos,
            List<Item> ultimasAutorizacoes) {
    }

    private final PainelConsulta consulta;

    public ObterResumoPainelUseCase(PainelConsulta consulta) {
        this.consulta = consulta;
    }

    public Resumo executar() {
        List<CoberturaIdade> cobertura = consulta.coberturaPorIdade();
        List<Item> ultimas = consulta.ultimasAutorizacoes(MAXIMO_ULTIMAS);
        if (ultimas.size() > MAXIMO_ULTIMAS) {
            ultimas = ultimas.subList(0, MAXIMO_ULTIMAS);
        }
        return new Resumo(consulta.totalProcedimentos(), consulta.totalAutorizacoes(), cobertura.size(),
                cobertura, consulta.distribuicaoSexos(), List.copyOf(ultimas));
    }
}
