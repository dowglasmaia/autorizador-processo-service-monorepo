package org.maia.autorizador.procedimento.api;

import java.net.URI;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.maia.autorizador.generated.procedimento.api.ProcedimentosApi;
import org.maia.autorizador.generated.procedimento.model.PaginaProcedimentos;
import org.maia.autorizador.generated.procedimento.model.Procedimento;
import org.maia.autorizador.generated.procedimento.model.ProcedimentoRequest;
import org.maia.autorizador.generated.procedimento.model.Sexo;
import org.maia.autorizador.procedimento.application.usecase.CadastrarProcedimentoUseCase;
import org.maia.autorizador.procedimento.application.usecase.ListarProcedimentosUseCase;
import org.maia.autorizador.procedimento.application.usecase.ObterProcedimentoUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador HTTP do catálogo de procedimentos sobre a interface gerada do contrato. Traduz o DTO gerado para o
 * caso de uso e de volta; a saída traz {@code sexos} na ordem M, F e {@code idades} em ordem crescente.
 */
@RestController
@RequestMapping("/api/v1")
class ProcedimentoController implements ProcedimentosApi {

    private static final String PREFIXO_RECURSO = "/api/v1/procedimentos/";
    private static final String ORDEM_CRESCENTE = "numProcedimento,asc";
    private static final String ORDEM_DECRESCENTE = "numProcedimento,desc";

    private final CadastrarProcedimentoUseCase cadastrar;
    private final ListarProcedimentosUseCase listar;
    private final ObterProcedimentoUseCase obter;

    ProcedimentoController(CadastrarProcedimentoUseCase cadastrar, ListarProcedimentosUseCase listar,
            ObterProcedimentoUseCase obter) {
        this.cadastrar = cadastrar;
        this.listar = listar;
        this.obter = obter;
    }

    @Override
    public ResponseEntity<Procedimento> cadastrarProcedimento(ProcedimentoRequest corpo) {
        Set<Sexo> sexos = corpo.getSexos() == null ? Set.of() : corpo.getSexos();
        Set<Integer> idades = corpo.getIdades() == null ? Set.of() : corpo.getIdades();
        if (sexos.stream().anyMatch(Objects::isNull) || idades.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Sexos e idades não aceitam valores nulos.");
        }
        var criado = cadastrar.executar(corpo.getNumProcedimento(), sexos.stream().map(Sexo::getValue).toList(),
                idades);
        return ResponseEntity.created(URI.create(PREFIXO_RECURSO + criado.id())).body(paraResposta(criado));
    }

    @Override
    public ResponseEntity<PaginaProcedimentos> listarProcedimentos(String q, Integer page, Integer size,
            String sort) {
        var resultado = listar.executar(q, ordemCrescente(sort), page, size);
        var itens = resultado.itens().stream().map(ProcedimentoController::paraResposta).toList();
        return ResponseEntity.ok(new PaginaProcedimentos(itens, resultado.pagina(), resultado.tamanho(),
                resultado.totalItens(), resultado.totalPaginas()));
    }

    @Override
    public ResponseEntity<Procedimento> obterProcedimento(Long id) {
        return ResponseEntity.ok(paraResposta(obter.executar(id)));
    }

    private static boolean ordemCrescente(String sort) {
        if (sort == null || ORDEM_CRESCENTE.equals(sort)) {
            return true;
        }
        if (ORDEM_DECRESCENTE.equals(sort)) {
            return false;
        }
        throw new IllegalArgumentException(
                "Ordenação inválida: use " + ORDEM_CRESCENTE + " ou " + ORDEM_DECRESCENTE + ".");
    }

    private static Procedimento paraResposta(ObterProcedimentoUseCase.Resultado r) {
        List<Sexo> sexos = r.sexos().stream().map(Sexo::fromValue).toList();
        return new Procedimento(r.id(), r.numProcedimento(), sexos, r.idades(),
                r.criadoEm().atOffset(ZoneOffset.UTC));
    }
}
