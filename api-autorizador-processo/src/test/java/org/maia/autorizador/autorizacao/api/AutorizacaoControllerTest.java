package org.maia.autorizador.autorizacao.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.maia.autorizador.autorizacao.application.usecase.ListarAutorizacoesUseCase;
import org.maia.autorizador.autorizacao.application.usecase.ListarAutorizacoesUseCase.Item;
import org.maia.autorizador.autorizacao.application.usecase.ListarAutorizacoesUseCase.PaginaResultado;
import org.maia.autorizador.autorizacao.application.usecase.SolicitarAutorizacaoUseCase;
import org.maia.autorizador.autorizacao.application.usecase.SolicitarAutorizacaoUseCase.Concedida;
import org.maia.autorizador.autorizacao.domain.exception.AutorizacaoNegadaException;
import org.maia.autorizador.procedimento.application.usecase.ObterProcedimentoUseCase.Resultado;
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

/** Camada HTTP das autorizações (sem segurança nem banco): contrato, validação e tradução de erros. */
@WebMvcTest(controllers = AutorizacaoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("local")
class AutorizacaoControllerTest {

    private static final String BASE = "/api/v1/autorizacoes";

    @Autowired
    MockMvc mvc;

    @MockitoBean
    SolicitarAutorizacaoUseCase solicitar;
    @MockitoBean
    ListarAutorizacoesUseCase listar;

    private ResultActions postar(String json) throws Exception {
        return mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private static Resultado procedimento() {
        return new Resultado(1, 4567, List.of("M", "F"), List.of("CRIANCA", "ADOLESCENTE"), Instant.parse("2026-01-02T03:04:05Z"));
    }

    @Test
    void solicitacaoRetorna201ComCorpoEDataEmUtc() throws Exception {
        when(solicitar.executar(4567, 20, "F")).thenReturn(
                new Concedida(7, procedimento(), true, Instant.parse("2026-03-04T05:06:07Z")));

        postar("{\"numProcedimento\":4567,\"idade\":20,\"sexo\":\"F\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.permitido").value(true))
                .andExpect(jsonPath("$.criadaEm").value("2026-03-04T05:06:07Z"))
                .andExpect(jsonPath("$.procedimento.numProcedimento").value(4567))
                .andExpect(jsonPath("$.procedimento.sexos[0]").value("M"))
                .andExpect(jsonPath("$.procedimento.faixasEtarias[1]").value("ADOLESCENTE"));
    }

    @Test
    void sexoMinusculoChegaAoCasoDeUso() throws Exception {
        when(solicitar.executar(4567, 20, "f")).thenReturn(new Concedida(1, procedimento(), true, null));

        postar("{\"numProcedimento\":4567,\"idade\":20,\"sexo\":\"f\"}").andExpect(status().isCreated())
                .andExpect(jsonPath("$.criadaEm").doesNotExist());
        verify(solicitar).executar(4567, 20, "f");
    }

    @Test
    void sexoInvalidoRetorna400() throws Exception {
        postar("{\"numProcedimento\":4567,\"idade\":20,\"sexo\":\"Masculino\"}").andExpect(status().isBadRequest());
        verifyNoInteractions(solicitar);
    }

    @Test
    void idadeForaDaFaixaRetorna400() throws Exception {
        postar("{\"numProcedimento\":4567,\"idade\":151,\"sexo\":\"M\"}").andExpect(status().isBadRequest());
        postar("{\"numProcedimento\":4567,\"idade\":-1,\"sexo\":\"M\"}").andExpect(status().isBadRequest());
        verifyNoInteractions(solicitar);
    }

    @Test
    void jsonMalformadoOuIncompletoRetorna400() throws Exception {
        postar("{\"numProcedimento\":").andExpect(status().isBadRequest());
        postar("{\"numProcedimento\":4567}").andExpect(status().isBadRequest());
        verifyNoInteractions(solicitar);
    }

    @Test
    void negativaRetorna403ComCodigo() throws Exception {
        when(solicitar.executar(1234, 30, "M")).thenThrow(AutorizacaoNegadaException.para(1234));

        postar("{\"numProcedimento\":1234,\"idade\":30,\"sexo\":\"M\"}").andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTORIZACAO_NEGADA"));
    }

    @Test
    void procedimentoInexistenteRetorna404() throws Exception {
        when(solicitar.executar(55555, 30, "M")).thenThrow(ProcedimentoNaoEncontradoException.porNumero(55555));

        postar("{\"numProcedimento\":55555,\"idade\":30,\"sexo\":\"M\"}").andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROCEDIMENTO_NAO_ENCONTRADO"));
    }

    @Test
    void listagemDevolvePaginaComPadroes() throws Exception {
        when(listar.executar(0, 20)).thenReturn(new PaginaResultado(
                List.of(new Item(3, procedimento(), true, Instant.parse("2026-03-04T05:06:07Z"))), 0, 20, 1, 1));

        mvc.perform(get(BASE)).andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].id").value(3))
                .andExpect(jsonPath("$.itens[0].procedimento.numProcedimento").value(4567))
                .andExpect(jsonPath("$.totalItens").value(1))
                .andExpect(jsonPath("$.totalPaginas").value(1));
    }

    @Test
    void listagemRejeitaTamanhoForaDaFaixa() throws Exception {
        mvc.perform(get(BASE).param("size", "201")).andExpect(status().isBadRequest());
        mvc.perform(get(BASE).param("page", "-1")).andExpect(status().isBadRequest());
        verifyNoInteractions(listar);
    }
}
