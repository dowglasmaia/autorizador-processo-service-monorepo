package org.maia.autorizador.painel.infrastructure.persistence;

import java.util.List;
import org.maia.autorizador.autorizacao.application.usecase.ListarAutorizacoesUseCase;
import org.maia.autorizador.autorizacao.application.usecase.ListarAutorizacoesUseCase.Item;
import org.maia.autorizador.painel.application.port.PainelConsulta;
import org.maia.autorizador.painel.application.model.CoberturaIdade;
import org.maia.autorizador.painel.application.model.DistribuicaoSexos;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador de leitura do painel: SQL agregado via {@link JdbcClient} (sem entidades) e as últimas autorizações
 * pela API pública do caso de uso de listagem (procedimentos em lote, sem N+1).
 */
@Component
@Transactional(readOnly = true)
class PainelConsultaAdapter implements PainelConsulta {

    private final JdbcClient jdbc;
    private final ListarAutorizacoesUseCase listar;

    PainelConsultaAdapter(JdbcClient jdbc, ListarAutorizacoesUseCase listar) {
        this.jdbc = jdbc;
        this.listar = listar;
    }

    @Override
    public int totalProcedimentos() {
        return jdbc.sql("select count(*) from procedimento").query(Integer.class).single();
    }

    @Override
    public int totalAutorizacoes() {
        return jdbc.sql("select count(*) from autorizacao").query(Integer.class).single();
    }

    @Override
    public List<CoberturaIdade> coberturaPorIdade() {
        return jdbc.sql("select idade, count(*) as quantidade from procedimento_idade group by idade order by idade")
                .query((rs, n) -> new CoberturaIdade(rs.getInt("idade"), rs.getInt("quantidade"))).list();
    }

    @Override
    public DistribuicaoSexos distribuicaoSexos() {
        int[] contagem = new int[2];
        jdbc.sql("select sexo, count(distinct procedimento_id) as quantidade from procedimento_sexo group by sexo")
                .query((rs, n) -> {
                    if ("M".equals(rs.getString("sexo"))) {
                        contagem[0] = rs.getInt("quantidade");
                    } else if ("F".equals(rs.getString("sexo"))) {
                        contagem[1] = rs.getInt("quantidade");
                    }
                    return null;
                }).list();
        return new DistribuicaoSexos(contagem[0], contagem[1]);
    }

    @Override
    public List<Item> ultimasAutorizacoes(int limite) {
        return listar.executar(0, limite).itens();
    }
}
