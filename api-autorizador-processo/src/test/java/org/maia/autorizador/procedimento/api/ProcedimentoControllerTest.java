package org.maia.autorizador.procedimento.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.maia.autorizador.procedimento.application.usecase.CadastrarProcedimentoUseCase;
import org.maia.autorizador.procedimento.application.usecase.ListarProcedimentosUseCase;
import org.maia.autorizador.procedimento.application.usecase.ListarProcedimentosUseCase.PaginaResultado;
import org.maia.autorizador.procedimento.application.usecase.ObterProcedimentoUseCase;
import org.maia.autorizador.procedimento.application.usecase.ObterProcedimentoUseCase.Catalogo;
import org.maia.autorizador.procedimento.application.usecase.ObterProcedimentoUseCase.Resultado;
import org.maia.autorizador.procedimento.domain.exception.ProcedimentoDuplicadoException;
import org.maia.autorizador.procedimento.domain.exception.ProcedimentoNaoEncontradoException;
import org.maia.autorizador.shared.web.GlobalExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Camada HTTP do catálogo (sem segurança nem banco): contrato, validação e tradução de erros. */
@WebMvcTest(controllers = ProcedimentoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, SexoJacksonConfig.class})
@ActiveProfiles("local")
class ProcedimentoControllerTest {

    private static final String BASE = "/api/v1/procedimentos";

    @Autowired
    MockMvc mvc;

    @MockitoBean
    CadastrarProcedimentoUseCase cadastrar;
    @MockitoBean
    ListarProcedimentosUseCase listar;
    @MockitoBean
    ObterProcedimentoUseCase obter;

    private ResultActions postar(String json) throws Exception {
        return mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private static Resultado salvo(long id, int numero, List<String> sexos, List<Integer> idades) {
        return new Resultado(id, numero, sexos, idades, Instant.parse("2026-01-02T03:04:05Z"));
    }

    @Test
    void cadastroRetorna201ComCorpoELocation() throws Exception {
        when(cadastrar.executar(eq(9999), any(), any(), any()))
                .thenReturn(salvo(42, 9999, List.of("M", "F"), List.of(18, 30)));

        postar("{\"nome\":\"Proc\",\"categoria\":\"CONSULTAS\",\"numProcedimento\":9999,\"sexos\":[\"F\",\"M\"],\"idades\":[30,18]}")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/procedimentos/42"))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.numProcedimento").value(9999))
                .andExpect(jsonPath("$.sexos[0]").value("M"))
                .andExpect(jsonPath("$.sexos[1]").value("F"))
                .andExpect(jsonPath("$.idades[0]").value(18))
                .andExpect(jsonPath("$.idades[1]").value(30))
                .andExpect(jsonPath("$.criadoEm").exists());
    }

    @Test
    void sexosEmMinusculasSaoAceitos() throws Exception {
        when(cadastrar.executar(eq(10), any(), any(), any())).thenReturn(salvo(1, 10, List.of("M", "F"), List.of()));

        postar("{\"nome\":\"Proc\",\"categoria\":\"CONSULTAS\",\"numProcedimento\":10,\"sexos\":[\"m\",\"f\"],\"idades\":[]}").andExpect(status().isCreated());
        verify(cadastrar).executar(eq(10), eq(List.of("M", "F")), any(), any());
    }

    @Test
    void listasAusentesOuVaziasSaoAceitas() throws Exception {
        when(cadastrar.executar(eq(11), any(), any(), any())).thenReturn(salvo(2, 11, List.of(), List.of()));

        postar("{\"nome\":\"Proc\",\"categoria\":\"CONSULTAS\",\"numProcedimento\":11}").andExpect(status().isCreated())
                .andExpect(jsonPath("$.sexos").isEmpty()).andExpect(jsonPath("$.idades").isEmpty());
    }

    @Test
    void entradaInvalidaRetorna400SemChamarOCasoDeUso() throws Exception {
        postar("{\"numProcedimento\":1,\"sexos\":[\"X\"],\"idades\":[1]}").andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("CORPO_INVALIDO"));
        postar("{\"numProcedimento\":0,\"sexos\":[\"M\"],\"idades\":[1]}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACAO_INVALIDA"));
        postar("{\"sexos\":[\"M\"]}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACAO_INVALIDA"));
        postar("{\"numProcedimento\":1,\"idades\":[151]}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACAO_INVALIDA"));
        postar("{\"numProcedimento\":1,\"idades\":[-1]}").andExpect(status().isBadRequest());
        postar("{\"numProcedimento\":1,\"sexos\":[null]}").andExpect(status().isBadRequest());
        postar("{\"numProcedimento\":").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CORPO_INVALIDO"));
        verifyNoInteractions(cadastrar);
    }

    private static final String BASE_JSON = "\"numProcedimento\":5,\"nome\":\"Eco\",\"categoria\":\"CONSULTAS\"";

    @Test
    void cadastroEstendidoMapeiaRequestEResponse() throws Exception {
        var catalogo = new Catalogo("Eco", "123", "EXAMES_IMAGEM", "CARDIOLOGIA", "REGRAS", 2, "ANO", true,
                false);
        when(cadastrar.executar(eq(5), any(), any(), any())).thenReturn(new Resultado(7, 5, List.of(), List.of(),
                Instant.parse("2026-01-02T03:04:05Z"), catalogo));

        postar("{\"numProcedimento\":5,\"nome\":\" Eco \",\"codigoTuss\":\"123\",\"categoria\":\"EXAMES_IMAGEM\","
                + "\"especialidade\":\"CARDIOLOGIA\",\"tipoCobertura\":\"REGRAS\",\"quantidadeMaxima\":2,"
                + "\"periodo\":\"ANO\",\"exigeAutorizacaoPrevia\":true}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Eco"))
                .andExpect(jsonPath("$.codigoTuss").value("123"))
                .andExpect(jsonPath("$.categoria").value("EXAMES_IMAGEM"))
                .andExpect(jsonPath("$.especialidade").value("CARDIOLOGIA"))
                .andExpect(jsonPath("$.tipoCobertura").value("REGRAS"))
                .andExpect(jsonPath("$.quantidadeMaxima").value(2))
                .andExpect(jsonPath("$.periodo").value("ANO"))
                .andExpect(jsonPath("$.exigeAutorizacaoPrevia").value(true))
                .andExpect(jsonPath("$.exigeJustificativaMedica").value(false));
        verify(cadastrar).executar(eq(5), any(), any(), eq(new Catalogo(" Eco ", "123",
                "EXAMES_IMAGEM", "CARDIOLOGIA", "REGRAS", 2, "ANO", true, null)));
    }

    @Test
    void getEListagemDevolvemCamposDeCatalogoENulosParaLinhasAntigas() throws Exception {
        var catalogo = new Catalogo("Eco", null, "CONSULTAS", null, "PLANO", null, null, false, false);
        var comCatalogo = new Resultado(1, 5, List.of(), List.of(), Instant.EPOCH, catalogo);
        when(obter.executar(1)).thenReturn(comCatalogo);
        when(listar.executar(any(), anyBoolean(), anyInt(), anyInt())).thenReturn(
                new PaginaResultado(List.of(comCatalogo, salvo(2, 1234, List.of(), List.of())), 0, 20, 2, 1));

        mvc.perform(get(BASE + "/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Eco")).andExpect(jsonPath("$.categoria").value("CONSULTAS"));
        mvc.perform(get(BASE)).andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].tipoCobertura").value("PLANO"))
                .andExpect(jsonPath("$.itens[1].nome").doesNotExist())
                .andExpect(jsonPath("$.itens[1].categoria").doesNotExist())
                .andExpect(jsonPath("$.itens[1].exigeAutorizacaoPrevia").doesNotExist());
    }

    @Test
    void nomeECategoriaSaoObrigatoriosComErroPorCampo() throws Exception {
        postar("{\"numProcedimento\":5,\"categoria\":\"CONSULTAS\"}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACAO_INVALIDA"))
                .andExpect(jsonPath("$.errors[0].campo").value("nome"));
        postar("{\"numProcedimento\":5,\"nome\":\"Eco\"}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACAO_INVALIDA"))
                .andExpect(jsonPath("$.errors[0].campo").value("categoria"));
        postar("{\"numProcedimento\":5,\"nome\":\"\",\"categoria\":\"CONSULTAS\"}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].campo").value("nome"));
        postar("{\"numProcedimento\":5,\"nome\":\"   \",\"categoria\":\"CONSULTAS\"}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDACAO_INVALIDA"))
                .andExpect(jsonPath("$.errors[0].campo").value("nome"));
        postar("{\"numProcedimento\":5,\"nome\":\"" + "a".repeat(201) + "\",\"categoria\":\"CONSULTAS\"}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors[0].campo").value("nome"));
        verifyNoInteractions(cadastrar);
    }

    @Test
    void quantidadeMaximaEPeriodoDevemVirJuntos() throws Exception {
        postar("{" + BASE_JSON + ",\"quantidadeMaxima\":2}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACAO_INVALIDA"))
                .andExpect(jsonPath("$.errors[0].campo").value("periodo"));
        postar("{" + BASE_JSON + ",\"periodo\":\"MES\"}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACAO_INVALIDA"))
                .andExpect(jsonPath("$.errors[0].campo").value("quantidadeMaxima"));
        postar("{" + BASE_JSON + ",\"quantidadeMaxima\":0,\"periodo\":\"MES\"}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].campo").value("quantidadeMaxima"));
        verifyNoInteractions(cadastrar);
    }

    @Test
    void valoresForaDosEnumsRetornam400() throws Exception {
        postar("{\"numProcedimento\":5,\"nome\":\"Eco\",\"categoria\":\"OUTRA\"}").andExpect(status().isBadRequest());
        postar("{" + BASE_JSON + ",\"especialidade\":\"X\"}").andExpect(status().isBadRequest());
        postar("{" + BASE_JSON + ",\"tipoCobertura\":\"X\"}").andExpect(status().isBadRequest());
        postar("{" + BASE_JSON + ",\"quantidadeMaxima\":1,\"periodo\":\"SEMANA\"}").andExpect(status().isBadRequest());
        verifyNoInteractions(cadastrar);
    }

    @Test
    void duplicidadeRetorna403() throws Exception {
        when(cadastrar.executar(eq(1234), any(), any(), any())).thenThrow(new ProcedimentoDuplicadoException(1234));

        postar("{\"nome\":\"Proc\",\"categoria\":\"CONSULTAS\",\"numProcedimento\":1234}").andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PROCEDIMENTO_DUPLICADO"));
    }

    @Test
    void obterInexistenteRetorna404() throws Exception {
        when(obter.executar(99)).thenThrow(ProcedimentoNaoEncontradoException.porId(99));

        mvc.perform(get(BASE + "/99")).andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("PROCEDIMENTO_NAO_ENCONTRADO"));
    }

    @Test
    void obterComIdNaoNumericoRetorna400() throws Exception {
        mvc.perform(get(BASE + "/abc")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PARAMETRO_INVALIDO"));
    }

    @Test
    void obterExistenteRetorna200() throws Exception {
        when(obter.executar(5)).thenReturn(salvo(5, 1234, List.of("F"), List.of(10, 20)));

        mvc.perform(get(BASE + "/5")).andExpect(status().isOk())
                .andExpect(jsonPath("$.numProcedimento").value(1234))
                .andExpect(jsonPath("$.idades[1]").value(20));
    }

    @Test
    void listagemUsaPadroesEOrdenacaoDescendente() throws Exception {
        when(listar.executar(any(), anyBoolean(), anyInt(), anyInt())).thenReturn(
                new PaginaResultado(List.of(salvo(1, 6789, List.of("M"), List.of(10))), 0, 20, 1, 1));

        mvc.perform(get(BASE)).andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].numProcedimento").value(6789))
                .andExpect(jsonPath("$.totalItens").value(1))
                .andExpect(jsonPath("$.totalPaginas").value(1));
        verify(listar).executar(null, true, 0, 20);

        mvc.perform(get(BASE).param("q", "67").param("page", "2").param("size", "5")
                .param("sort", "numProcedimento,desc")).andExpect(status().isOk());
        verify(listar).executar("67", false, 2, 5);
    }

    @Test
    void parametrosDeListagemInvalidosRetornam400() throws Exception {
        mvc.perform(get(BASE).param("q", "12a")).andExpect(status().isBadRequest());
        mvc.perform(get(BASE).param("size", "0")).andExpect(status().isBadRequest());
        mvc.perform(get(BASE).param("size", "201")).andExpect(status().isBadRequest());
        mvc.perform(get(BASE).param("page", "-1")).andExpect(status().isBadRequest());
        mvc.perform(get(BASE).param("sort", "nome,asc")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACAO_INVALIDA"));
        verify(listar, never()).executar(any(), anyBoolean(), anyInt(), anyInt());
    }
}
