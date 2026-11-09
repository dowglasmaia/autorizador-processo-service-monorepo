package org.maia.autorizador.painel.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.maia.autorizador.autorizacao.application.usecase.ListarAutorizacoesUseCase.Item;
import org.maia.autorizador.painel.application.usecase.ObterResumoPainelUseCase;
import org.maia.autorizador.painel.application.usecase.ObterResumoPainelUseCase.Resumo;
import org.maia.autorizador.painel.application.model.CoberturaFaixa;
import org.maia.autorizador.painel.application.model.DistribuicaoSexos;
import org.maia.autorizador.procedimento.domain.model.FaixaEtaria;
import org.maia.autorizador.procedimento.application.usecase.ObterProcedimentoUseCase.Resultado;
import org.maia.autorizador.shared.web.GlobalExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Camada HTTP do painel (sem segurança nem banco): corpo completo do resumo. */
@WebMvcTest(controllers = PainelController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("local")
class PainelControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ObterResumoPainelUseCase obterResumo;

    @Test
    void devolve200ComCorpoCompleto() throws Exception {
        var procedimento = new Resultado(1, 4567, List.of("M", "F"), List.of("ADOLESCENTE", "ADULTO"),
                Instant.parse("2026-01-02T03:04:05Z"));
        when(obterResumo.executar()).thenReturn(new Resumo(3, 2, 3,
                List.of(new CoberturaFaixa("CRIANCA", 2), new CoberturaFaixa("ADOLESCENTE", 2), new CoberturaFaixa("ADULTO", 1)),
                new DistribuicaoSexos(2, 3),
                List.of(new Item(9, procedimento, true, Instant.parse("2026-03-04T05:06:07Z")),
                        new Item(8, procedimento, true, null))));

        mvc.perform(get("/api/v1/painel/resumo")).andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProcedimentos").value(3))
                .andExpect(jsonPath("$.totalAutorizacoes").value(2))
                .andExpect(jsonPath("$.faixasCobertas").value(3))
                .andExpect(jsonPath("$.coberturaPorFaixa[0].faixa").value("CRIANCA"))
                .andExpect(jsonPath("$.coberturaPorFaixa[2].quantidade").value(1))
                .andExpect(jsonPath("$.distribuicaoSexos.masculino").value(2))
                .andExpect(jsonPath("$.distribuicaoSexos.feminino").value(3))
                .andExpect(jsonPath("$.ultimasAutorizacoes[0].id").value(9))
                .andExpect(jsonPath("$.ultimasAutorizacoes[0].criadaEm").value("2026-03-04T05:06:07Z"))
                .andExpect(jsonPath("$.ultimasAutorizacoes[0].procedimento.numProcedimento").value(4567))
                .andExpect(jsonPath("$.ultimasAutorizacoes[0].procedimento.sexos[1]").value("F"))
                .andExpect(jsonPath("$.ultimasAutorizacoes[1].criadaEm").doesNotExist());
    }

    @Test
    void devolveListasVaziasEZeros() throws Exception {
        when(obterResumo.executar()).thenReturn(
                new Resumo(0, 0, 0, List.of(), new DistribuicaoSexos(0, 0), List.of()));

        mvc.perform(get("/api/v1/painel/resumo")).andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProcedimentos").value(0))
                .andExpect(jsonPath("$.coberturaPorFaixa").isEmpty())
                .andExpect(jsonPath("$.ultimasAutorizacoes").isEmpty());
    }
}
