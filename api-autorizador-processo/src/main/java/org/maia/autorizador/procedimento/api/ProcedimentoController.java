package org.maia.autorizador.procedimento.api;

import java.net.URI;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.maia.autorizador.generated.procedimento.api.ProcedimentosApi;
import org.maia.autorizador.generated.procedimento.model.PaginaProcedimentos;
import org.maia.autorizador.generated.procedimento.model.Categoria;
import org.maia.autorizador.generated.procedimento.model.Especialidade;
import org.maia.autorizador.generated.procedimento.model.Periodo;
import org.maia.autorizador.generated.procedimento.model.Procedimento;
import org.maia.autorizador.generated.procedimento.model.ProcedimentoRequest;
import org.maia.autorizador.generated.procedimento.model.Sexo;
import org.maia.autorizador.generated.procedimento.model.TipoCobertura;
import org.maia.autorizador.procedimento.application.usecase.CadastrarProcedimentoUseCase;
import org.maia.autorizador.procedimento.application.usecase.ListarProcedimentosUseCase;
import org.maia.autorizador.procedimento.application.usecase.ObterProcedimentoUseCase;
import org.maia.autorizador.procedimento.application.usecase.ObterProcedimentoUseCase.Catalogo;
import org.maia.autorizador.shared.error.CampoInvalidoException;
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
                idades, paraCatalogo(corpo));
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

    /** Valida o que o Bean Validation não cobre e aponta o campo no erro. */
    private static Catalogo paraCatalogo(ProcedimentoRequest corpo) {
        if (corpo.getNome().isBlank()) {
            throw new CampoInvalidoException("nome", "O nome deve ter de 1 a 200 caracteres.");
        }
        if (corpo.getQuantidadeMaxima() != null && corpo.getPeriodo() == null) {
            throw new CampoInvalidoException("periodo", "Informe o período junto com a quantidade máxima.");
        }
        if (corpo.getQuantidadeMaxima() == null && corpo.getPeriodo() != null) {
            throw new CampoInvalidoException("quantidadeMaxima",
                    "Informe a quantidade máxima junto com o período.");
        }
        return new Catalogo(corpo.getNome(), corpo.getCodigoTuss(), texto(corpo.getCategoria()),
                texto(corpo.getEspecialidade()), texto(corpo.getTipoCobertura()), corpo.getQuantidadeMaxima(),
                texto(corpo.getPeriodo()), corpo.getExigeAutorizacaoPrevia(), corpo.getExigeJustificativaMedica());
    }

    /** Os enums gerados e os do módulo têm os mesmos nomes; a troca é feita pelo nome do valor. */
    private static String texto(Enum<?> valor) {
        return valor == null ? null : valor.name();
    }

    private static <E extends Enum<E>> E enumGerado(Class<E> tipo, String nome) {
        return nome == null ? null : Enum.valueOf(tipo, nome);
    }

    private static Procedimento paraResposta(ObterProcedimentoUseCase.Resultado r) {
        List<Sexo> sexos = r.sexos().stream().map(Sexo::fromValue).toList();
        var resposta = new Procedimento(r.id(), r.numProcedimento(), sexos, r.idades(),
                r.criadoEm().atOffset(ZoneOffset.UTC));
        Catalogo c = r.catalogo();
        resposta.setNome(c.nome());
        resposta.setCodigoTuss(c.codigoTuss());
        resposta.setCategoria(enumGerado(Categoria.class, c.categoria()));
        resposta.setEspecialidade(enumGerado(Especialidade.class, c.especialidade()));
        resposta.setTipoCobertura(enumGerado(TipoCobertura.class, c.tipoCobertura()));
        resposta.setQuantidadeMaxima(c.quantidadeMaxima());
        resposta.setPeriodo(enumGerado(Periodo.class, c.periodo()));
        resposta.setExigeAutorizacaoPrevia(c.exigeAutorizacaoPrevia());
        resposta.setExigeJustificativaMedica(c.exigeJustificativaMedica());
        return resposta;
    }
}
