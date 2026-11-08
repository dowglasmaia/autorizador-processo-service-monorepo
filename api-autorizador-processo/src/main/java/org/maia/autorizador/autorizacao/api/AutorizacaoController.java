package org.maia.autorizador.autorizacao.api;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.maia.autorizador.autorizacao.application.usecase.ListarAutorizacoesUseCase;
import org.maia.autorizador.autorizacao.application.usecase.SolicitarAutorizacaoUseCase;
import org.maia.autorizador.generated.autorizacao.api.AutorizacoesApi;
import org.maia.autorizador.generated.autorizacao.model.Autorizacao;
import org.maia.autorizador.generated.autorizacao.model.AutorizacaoProcedimento;
import org.maia.autorizador.generated.autorizacao.model.AutorizacaoRequest;
import org.maia.autorizador.generated.autorizacao.model.PaginaAutorizacoes;
import org.maia.autorizador.procedimento.application.usecase.ObterProcedimentoUseCase.Catalogo;
import org.maia.autorizador.procedimento.application.usecase.ObterProcedimentoUseCase.Resultado;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador HTTP das autorizações sobre a interface gerada do contrato. Monta o {@code Procedimento} gerado a
 * partir de {@link Resultado}/{@link Catalogo}, sem tocar o domínio de {@code procedimento}.
 */
@RestController
@RequestMapping("/api/v1")
class AutorizacaoController implements AutorizacoesApi {

    private final SolicitarAutorizacaoUseCase solicitar;
    private final ListarAutorizacoesUseCase listar;

    AutorizacaoController(SolicitarAutorizacaoUseCase solicitar, ListarAutorizacoesUseCase listar) {
        this.solicitar = solicitar;
        this.listar = listar;
    }

    @Override
    public ResponseEntity<Autorizacao> solicitarAutorizacao(AutorizacaoRequest corpo) {
        var concedida = solicitar.executar(corpo.getNumProcedimento(), corpo.getIdade(), corpo.getSexo());
        return ResponseEntity.status(HttpStatus.CREATED).body(paraResposta(concedida.id(),
                concedida.procedimento(), concedida.permitido(), concedida.criadaEm()));
    }

    @Override
    public ResponseEntity<PaginaAutorizacoes> listarAutorizacoes(Integer page, Integer size) {
        var resultado = listar.executar(page, size);
        List<Autorizacao> itens = resultado.itens().stream()
                .map(i -> paraResposta(i.id(), i.procedimento(), i.permitido(), i.criadaEm())).toList();
        return ResponseEntity.ok(new PaginaAutorizacoes(itens, resultado.pagina(), resultado.tamanho(),
                resultado.totalItens(), resultado.totalPaginas()));
    }

    private static Autorizacao paraResposta(long id, Resultado procedimento, boolean permitido, Instant criadaEm) {
        OffsetDateTime data = criadaEm == null ? null : criadaEm.atOffset(ZoneOffset.UTC);
        return new Autorizacao(id, paraProcedimento(procedimento), permitido, data);
    }

    private static AutorizacaoProcedimento paraProcedimento(Resultado r) {
        List<AutorizacaoProcedimento.SexosEnum> sexos = r.sexos().stream()
                .map(AutorizacaoProcedimento.SexosEnum::fromValue).toList();
        var p = new AutorizacaoProcedimento(r.id(), r.numProcedimento(), sexos, r.idades(),
                r.criadoEm().atOffset(ZoneOffset.UTC));
        Catalogo c = r.catalogo();
        p.setNome(c.nome());
        p.setCodigoTuss(c.codigoTuss());
        p.setCategoria(c.categoria() == null ? null
                : AutorizacaoProcedimento.CategoriaEnum.fromValue(c.categoria()));
        p.setEspecialidade(c.especialidade() == null ? null
                : AutorizacaoProcedimento.EspecialidadeEnum.fromValue(c.especialidade()));
        p.setTipoCobertura(c.tipoCobertura() == null ? null
                : AutorizacaoProcedimento.TipoCoberturaEnum.fromValue(c.tipoCobertura()));
        p.setQuantidadeMaxima(c.quantidadeMaxima());
        p.setPeriodo(c.periodo() == null ? null : AutorizacaoProcedimento.PeriodoEnum.fromValue(c.periodo()));
        p.setExigeAutorizacaoPrevia(c.exigeAutorizacaoPrevia());
        p.setExigeJustificativaMedica(c.exigeJustificativaMedica());
        return p;
    }
}
