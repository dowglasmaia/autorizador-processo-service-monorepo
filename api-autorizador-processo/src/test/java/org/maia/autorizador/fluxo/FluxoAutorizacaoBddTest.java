package org.maia.autorizador.fluxo;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.maia.autorizador.support.IntegrationTestBase;
import org.maia.autorizador.support.JwtTestSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Teste documental (estilo BDD: Dado / Quando / Então) do fluxo completo de negócio, com a <b>massa de dados</b>
 * explícita. Serve de roteiro para reproduzir manualmente (Swagger, Postman, front) o mesmo caminho.
 *
 * <p>Regras que o roteiro demonstra (ver também o README, seção "Massa de teste e fluxo de ponta a ponta"):
 * <ul>
 *   <li>a regra de idade é por <b>faixa etária</b>: CRIANCA (0–12), ADOLESCENTE (13–20, isto é, de 12 anos e 1 dia
 *       até 20 anos), ADULTO (21–59) e IDOSO (60–150). O procedimento 500 aceita CRIANCA e IDOSO: autoriza 8 e 70
 *       anos, e nega 35 (adulto);</li>
 *   <li>autoriza se o sexo está entre os aceitos <b>e</b> a faixa da idade informada está entre as aceitas
 *       (RN5–RN7);</li>
 *   <li>toda negativa de <b>negócio</b> responde <b>403</b> com {@code code} no corpo
 *       ({@code AUTORIZACAO_NEGADA}, {@code PROCEDIMENTO_DUPLICADO}) — não é falta de permissão do token;</li>
 *   <li>negativa não grava nada (RN8); número de procedimento é único e imutável; não existe PUT/PATCH.</li>
 * </ul>
 * O banco começa vazio em cada teste (sem a seed local): a massa é criada no "Dado".
 */
@AutoConfigureMockMvc
@DisplayName("Fluxo de autorização — roteiro BDD com massa explícita")
class FluxoAutorizacaoBddTest extends IntegrationTestBase {

    // ---- MASSA DE TESTE ------------------------------------------------------------------------------------
    /** Procedimento de exemplo: aceita M e F, nas faixas CRIANCA (0–12) e IDOSO (60+). */
    private static final int NUM_PROCEDIMENTO = 500;

    private static final String CADASTRO_VALIDO = """
            {"numProcedimento":500,"nome":"Ressonância magnética de joelho","categoria":"EXAMES_IMAGEM",
             "sexos":["M","F"],"faixasEtarias":["CRIANCA","IDOSO"]}""";

    private static final String SOLICITACAO_8_F = "{\"numProcedimento\":500,\"idade\":8,\"sexo\":\"F\"}";
    private static final String SOLICITACAO_35_F = "{\"numProcedimento\":500,\"idade\":35,\"sexo\":\"F\"}";
    private static final String SOLICITACAO_INEXISTENTE = "{\"numProcedimento\":999,\"idade\":30,\"sexo\":\"F\"}";

    /** Procedimento só feminino, para o cenário de sexo não aceito. */
    private static final String CADASTRO_SO_FEMININO = """
            {"numProcedimento":501,"nome":"Mamografia","categoria":"EXAMES_IMAGEM","sexos":["F"],"faixasEtarias":["ADULTO"]}""";
    private static final String SOLICITACAO_501_M = "{\"numProcedimento\":501,\"idade\":40,\"sexo\":\"M\"}";
    // --------------------------------------------------------------------------------------------------------

    @Autowired
    MockMvc mvc;

    private ResultActions enviar(String caminho, String json) throws Exception {
        return mvc.perform(post(caminho).header(HttpHeaders.AUTHORIZATION, bearer())
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private ResultActions consultar(String caminho) throws Exception {
        return mvc.perform(get(caminho).header(HttpHeaders.AUTHORIZATION, bearer()));
    }

    private static String bearer() {
        return JwtTestSupport.cabecalhoBearer(1L, "analista@exemplo.com");
    }

    @Nested
    @DisplayName("Cenário: cadastrar procedimento")
    class Cadastro {

        @Test
        @DisplayName("Dado um token válido e dados completos, quando cadastro o procedimento 500, então 201 com as faixas etárias")
        void cadastraComSucesso() throws Exception {
            enviar("/api/v1/procedimentos", CADASTRO_VALIDO).andExpect(status().isCreated())
                    .andExpect(jsonPath("$.numProcedimento").value(NUM_PROCEDIMENTO))
                    .andExpect(jsonPath("$.faixasEtarias[0]").value("CRIANCA"))
                    .andExpect(jsonPath("$.faixasEtarias[1]").value("IDOSO"));
        }

        @Test
        @DisplayName("Dado que o 500 já existe, quando cadastro de novo, então 403 PROCEDIMENTO_DUPLICADO (regra de negócio, não permissão)")
        void numeroDuplicadoRetorna403() throws Exception {
            enviar("/api/v1/procedimentos", CADASTRO_VALIDO).andExpect(status().isCreated());

            enviar("/api/v1/procedimentos", CADASTRO_VALIDO).andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("PROCEDIMENTO_DUPLICADO"));
        }

        @Test
        @DisplayName("Dado um corpo sem os campos obrigatórios (nome e categoria), quando cadastro, então 400 e não 403")
        void semCamposObrigatoriosRetorna400() throws Exception {
            enviar("/api/v1/procedimentos", "{\"numProcedimento\":502,\"sexos\":[\"M\"],\"idades\":[10]}")
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Dado um procedimento existente, quando tento atualizá-lo com PUT, então 405: o contrato não tem atualização")
        void atualizacaoNaoExiste() throws Exception {
            enviar("/api/v1/procedimentos", CADASTRO_VALIDO).andExpect(status().isCreated());

            mvc.perform(put("/api/v1/procedimentos/1").header(HttpHeaders.AUTHORIZATION, bearer())
                    .contentType(MediaType.APPLICATION_JSON).content(CADASTRO_VALIDO))
                    .andExpect(status().isMethodNotAllowed());
        }
    }

    @Nested
    @DisplayName("Cenário: solicitar autorização")
    class Solicitacao {

        @Test
        @DisplayName("Dado o 500 (M/F; criança e idoso), quando solicito 8 anos F, então 201 permitido e aparece na listagem")
        void idadeESexoAceitosAutoriza() throws Exception {
            enviar("/api/v1/procedimentos", CADASTRO_VALIDO).andExpect(status().isCreated());

            enviar("/api/v1/autorizacoes", SOLICITACAO_8_F).andExpect(status().isCreated())
                    .andExpect(jsonPath("$.permitido").value(true))
                    .andExpect(jsonPath("$.procedimento.numProcedimento").value(NUM_PROCEDIMENTO));

            consultar("/api/v1/autorizacoes").andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalItens").value(1))
                    .andExpect(jsonPath("$.itens", hasSize(1)));
        }

        @Test
        @DisplayName("Dado o 500 (criança e idoso), quando solicito 35 anos (adulto), então 403 AUTORIZACAO_NEGADA")
        void idadeForaDasFaixasAceitasNega() throws Exception {
            enviar("/api/v1/procedimentos", CADASTRO_VALIDO).andExpect(status().isCreated());

            enviar("/api/v1/autorizacoes", SOLICITACAO_35_F).andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("AUTORIZACAO_NEGADA"));

            consultar("/api/v1/autorizacoes").andExpect(jsonPath("$.totalItens").value(0));
        }

        @Test
        @DisplayName("Dado o 500 (criança e idoso), então 12 e 60 anos autorizam e 13 e 59 anos negam (limites das faixas)")
        void limitesDasFaixas() throws Exception {
            enviar("/api/v1/procedimentos", CADASTRO_VALIDO).andExpect(status().isCreated());

            for (int idade : new int[] {0, 12, 60, 150}) {
                enviar("/api/v1/autorizacoes", "{\"numProcedimento\":500,\"idade\":" + idade + ",\"sexo\":\"M\"}")
                        .andExpect(status().isCreated());
            }
            for (int idade : new int[] {13, 20, 21, 59}) {
                enviar("/api/v1/autorizacoes", "{\"numProcedimento\":500,\"idade\":" + idade + ",\"sexo\":\"M\"}")
                        .andExpect(status().isForbidden());
            }
        }

        @Test
        @DisplayName("Dado o 501 (só F, adulto), quando solicito sexo M com idade de adulto, então 403 AUTORIZACAO_NEGADA")
        void sexoNaoAceitoNega() throws Exception {
            enviar("/api/v1/procedimentos", CADASTRO_SO_FEMININO).andExpect(status().isCreated());

            enviar("/api/v1/autorizacoes", SOLICITACAO_501_M).andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("AUTORIZACAO_NEGADA"));
        }

        @Test
        @DisplayName("Dado que o 999 não existe, quando solicito, então 404 (cadastre o procedimento antes)")
        void procedimentoInexistenteRetorna404() throws Exception {
            enviar("/api/v1/autorizacoes", SOLICITACAO_INEXISTENTE).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Dado o 500, quando solicito com sexo inválido, então 400")
        void sexoInvalidoRetorna400() throws Exception {
            enviar("/api/v1/procedimentos", CADASTRO_VALIDO).andExpect(status().isCreated());

            enviar("/api/v1/autorizacoes", "{\"numProcedimento\":500,\"idade\":30,\"sexo\":\"X\"}")
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Dado procedimento autorizado, quando consulto o painel, então 200")
        void painelResponde() throws Exception {
            enviar("/api/v1/procedimentos", CADASTRO_VALIDO).andExpect(status().isCreated());
            enviar("/api/v1/autorizacoes", SOLICITACAO_8_F).andExpect(status().isCreated());

            consultar("/api/v1/painel/resumo").andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Cenário: autenticação (401 é do token; 403 de negócio é outra coisa)")
    class Seguranca {

        @Test
        @DisplayName("Dado nenhum token, quando cadastro, então 401 (não 403)")
        void semTokenRetorna401() throws Exception {
            mvc.perform(post("/api/v1/procedimentos").contentType(MediaType.APPLICATION_JSON)
                    .content(CADASTRO_VALIDO)).andExpect(status().isUnauthorized());
        }
    }
}
