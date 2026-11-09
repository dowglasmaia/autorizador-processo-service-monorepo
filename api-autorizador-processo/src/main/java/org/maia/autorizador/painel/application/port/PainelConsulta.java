package org.maia.autorizador.painel.application.port;

import java.util.List;
import org.maia.autorizador.painel.application.model.CoberturaFaixa;
import org.maia.autorizador.painel.application.model.DistribuicaoSexos;
import org.maia.autorizador.autorizacao.application.usecase.ListarAutorizacoesUseCase.Item;

/** Porta de saída (somente leitura) com as consultas agregadas do painel. */
public interface PainelConsulta {

    int totalProcedimentos();

    int totalAutorizacoes();

    /** Só as faixas com ao menos um procedimento, da mais nova para a mais velha. */
    List<CoberturaFaixa> coberturaPorFaixa();

    /** Zero para o sexo ausente. */
    DistribuicaoSexos distribuicaoSexos();

    /** As autorizações mais recentes primeiro, no máximo {@code limite}. */
    List<Item> ultimasAutorizacoes(int limite);
}
