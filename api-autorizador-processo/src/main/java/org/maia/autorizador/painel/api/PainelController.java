package org.maia.autorizador.painel.api;

import java.time.ZoneOffset;
import java.util.List;
import org.maia.autorizador.autorizacao.application.usecase.ListarAutorizacoesUseCase.Item;
import org.maia.autorizador.generated.painel.api.PainelApi;
import org.maia.autorizador.generated.painel.model.CoberturaFaixa;
import org.maia.autorizador.generated.painel.model.DistribuicaoSexos;
import org.maia.autorizador.generated.painel.model.ResumoPainel;
import org.maia.autorizador.generated.painel.model.ResumoPainelUltimasAutorizacoesInner;
import org.maia.autorizador.generated.painel.model.ResumoPainelUltimasAutorizacoesInnerProcedimento;
import org.maia.autorizador.painel.application.usecase.ObterResumoPainelUseCase;
import org.maia.autorizador.painel.application.usecase.ObterResumoPainelUseCase.Resumo;
import org.maia.autorizador.procedimento.application.usecase.ObterProcedimentoUseCase.Catalogo;
import org.maia.autorizador.procedimento.application.usecase.ObterProcedimentoUseCase.Resultado;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador HTTP do painel sobre a interface gerada; o mapeamento para os tipos gerados é próprio do módulo. */
@RestController
@RequestMapping("/api/v1")
class PainelController implements PainelApi {

    private final ObterResumoPainelUseCase obterResumo;

    PainelController(ObterResumoPainelUseCase obterResumo) {
        this.obterResumo = obterResumo;
    }

    @Override
    public ResponseEntity<ResumoPainel> obterResumoPainel() {
        Resumo r = obterResumo.executar();
        List<CoberturaFaixa> cobertura = r.coberturaPorFaixa().stream()
                .map(c -> new CoberturaFaixa(CoberturaFaixa.FaixaEnum.fromValue(c.faixa()), c.quantidade())).toList();
        List<ResumoPainelUltimasAutorizacoesInner> ultimas = r.ultimasAutorizacoes().stream()
                .map(PainelController::paraAutorizacao).toList();
        return ResponseEntity.ok(new ResumoPainel(r.totalProcedimentos(), r.totalAutorizacoes(), r.faixasCobertas(),
                cobertura, new DistribuicaoSexos(r.distribuicaoSexos().masculino(), r.distribuicaoSexos().feminino()),
                ultimas));
    }

    private static ResumoPainelUltimasAutorizacoesInner paraAutorizacao(Item i) {
        var data = i.criadaEm() == null ? null : i.criadaEm().atOffset(ZoneOffset.UTC);
        return new ResumoPainelUltimasAutorizacoesInner(i.id(), paraProcedimento(i.procedimento()), i.permitido(),
                data);
    }

    private static ResumoPainelUltimasAutorizacoesInnerProcedimento paraProcedimento(Resultado r) {
        var sexos = r.sexos().stream().map(ResumoPainelUltimasAutorizacoesInnerProcedimento.SexosEnum::fromValue)
                .toList();
        var faixas = r.faixasEtarias().stream()
                .map(ResumoPainelUltimasAutorizacoesInnerProcedimento.FaixasEtariasEnum::fromValue).toList();
        var p = new ResumoPainelUltimasAutorizacoesInnerProcedimento(r.id(), r.numProcedimento(), sexos, faixas,
                r.criadoEm().atOffset(ZoneOffset.UTC));
        Catalogo c = r.catalogo();
        p.setNome(c.nome());
        p.setCodigoTuss(c.codigoTuss());
        p.setCategoria(c.categoria() == null ? null
                : ResumoPainelUltimasAutorizacoesInnerProcedimento.CategoriaEnum.fromValue(c.categoria()));
        p.setEspecialidade(c.especialidade() == null ? null
                : ResumoPainelUltimasAutorizacoesInnerProcedimento.EspecialidadeEnum.fromValue(c.especialidade()));
        p.setTipoCobertura(c.tipoCobertura() == null ? null
                : ResumoPainelUltimasAutorizacoesInnerProcedimento.TipoCoberturaEnum.fromValue(c.tipoCobertura()));
        p.setQuantidadeMaxima(c.quantidadeMaxima());
        p.setPeriodo(c.periodo() == null ? null
                : ResumoPainelUltimasAutorizacoesInnerProcedimento.PeriodoEnum.fromValue(c.periodo()));
        p.setExigeAutorizacaoPrevia(c.exigeAutorizacaoPrevia());
        p.setExigeJustificativaMedica(c.exigeJustificativaMedica());
        return p;
    }
}
