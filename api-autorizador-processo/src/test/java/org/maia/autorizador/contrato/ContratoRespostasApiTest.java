package org.maia.autorizador.contrato;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.maia.autorizador.autenticacao.application.port.HashSenha;
import org.maia.autorizador.autenticacao.application.port.UsuarioRepository;
import org.maia.autorizador.autenticacao.domain.model.Usuario;
import org.maia.autorizador.support.IntegrationTestBase;
import org.maia.autorizador.support.JwtTestSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.json.JsonMapper;

/**
 * Conformidade das respostas reais com o contrato OpenAPI (SPEC-32): cada uma das 8 operações é chamada com a
 * aplicação completa (segurança, controllers, casos de uso e PostgreSQL real) e a resposta (status, cabeçalhos
 * declarados, tipo de conteúdo e corpo) é validada contra o esquema do YAML por {@link ContratoOpenApi}.
 *
 * <p>Os 12 {@code code} de erro do contrato estão distribuídos assim: os 10 alcançáveis por rota são provocados
 * aqui; {@code ERRO_INTERNO} em {@link ContratoErroInternoApiTest}; {@code ACESSO_NEGADO}, que nenhuma rota atual
 * produz (não há papéis), em {@code AcessoNegadoContratoTest}. {@link #todosOsCodesDoContratoTemCobertura()} falha se
 * o contrato ganhar um {@code code} sem cobertura.
 */
@AutoConfigureMockMvc
class ContratoRespostasApiTest extends IntegrationTestBase {

    private static final String SENHA = "senha-de-teste-123";
    private static final Set<String> COBERTOS_AQUI = Set.of("VALIDACAO_INVALIDA", "CORPO_INVALIDO",
            "PARAMETRO_INVALIDO", "PROCEDIMENTO_DUPLICADO", "AUTORIZACAO_NEGADA", "PROCEDIMENTO_NAO_ENCONTRADO",
            "CREDENCIAIS_INVALIDAS", "NAO_AUTENTICADO", "SESSAO_EXPIRADA", "MUITAS_TENTATIVAS");
    private static final Set<String> COBERTOS_EM_OUTROS_TESTES = Set.of("ERRO_INTERNO", "ACESSO_NEGADO");

    private static final JsonMapper JSON = new JsonMapper();
    private final ContratoOpenApi contrato = new ContratoOpenApi();

    @Autowired
    MockMvc mvc;
    @Autowired
    DataSource dataSource;
    @Autowired
    UsuarioRepository usuarios;
    @Autowired
    HashSenha hashSenha;

    private String email;
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        new ResourceDatabasePopulator(new ClassPathResource("db/seed/V1000__seed_local.sql")).execute(dataSource);
        email = "c" + UUID.randomUUID().toString().substring(0, 8) + "@ex.com";
        usuario = usuarios.salvar(Usuario.novo(email, hashSenha.gerar(SENHA)));
    }

    /** Executa a requisição, valida a resposta contra o contrato e devolve o corpo já interpretado. */
    private Object chamar(MockHttpServletRequestBuilder req, String metodo, String rotaContrato, int statusEsperado,
            String codeEsperado) throws Exception {
        MockHttpServletResponse r = mvc.perform(req).andReturn().getResponse();
        String texto = r.getContentAsString(StandardCharsets.UTF_8);
        Object corpo = texto.isBlank() ? null : JSON.readValue(texto, Object.class);
        Map<String, String> cabecalhos = new LinkedHashMap<>();
        r.getHeaderNames().forEach(n -> cabecalhos.put(n, r.getHeader(n)));

        assertThat(r.getStatus()).as("status de %s %s: %s", metodo, rotaContrato, texto).isEqualTo(statusEsperado);
        assertThat(contrato.validarResposta(metodo, rotaContrato, r.getStatus(), r.getContentType(), cabecalhos,
                corpo)).as("violações do contrato em %s %s (%s)", metodo, rotaContrato, texto).isEmpty();
        if (codeEsperado != null) {
            String code = (String) ((Map<?, ?>) corpo).get("code");
            assertThat(code).isEqualTo(codeEsperado);
        }
        return corpo;
    }

    private MockHttpServletRequestBuilder autenticado(MockHttpServletRequestBuilder req) {
        return req.header(HttpHeaders.AUTHORIZATION, JwtTestSupport.cabecalhoBearer(usuario.id(), email));
    }

    private MockHttpServletRequestBuilder login(String emailLogin, String senha, String ip) {
        return post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + emailLogin + "\",\"senha\":\"" + senha + "\"}")
                .with(r -> {
                    r.setRemoteAddr(ip);
                    return r;
                });
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder req, String corpo) {
        return req.contentType(MediaType.APPLICATION_JSON).content(corpo);
    }

    // ---------------------------------------------------------------- auth

    @Test
    void autenticar() throws Exception {
        chamar(login(email, SENHA, "10.9.0.1"), "post", "/auth/login", 200, null);
        chamar(login(email, "errada", "10.9.0.2"), "post", "/auth/login", 401, "CREDENCIAIS_INVALIDAS");
        chamar(json(post("/api/v1/auth/login"), "{\"email\":\"nao-e-email\",\"senha\":\"\"}"), "post", "/auth/login",
                400, "VALIDACAO_INVALIDA");
        chamar(json(post("/api/v1/auth/login"), "{email"), "post", "/auth/login", 400, "CORPO_INVALIDO");
    }

    @Test
    void autenticarMuitasTentativas() throws Exception {
        for (int i = 0; i < 5; i++) {
            chamar(login(email, "errada", "10.9.0.3"), "post", "/auth/login", 401, "CREDENCIAIS_INVALIDAS");
        }
        chamar(login(email, SENHA, "10.9.0.3"), "post", "/auth/login", 429, "MUITAS_TENTATIVAS");
    }

    @Test
    void obterUsuarioAtual() throws Exception {
        chamar(autenticado(get("/api/v1/auth/me")), "get", "/auth/me", 200, null);
        chamar(get("/api/v1/auth/me"), "get", "/auth/me", 401, "NAO_AUTENTICADO");
        chamar(get("/api/v1/auth/me").header(HttpHeaders.AUTHORIZATION,
                "Bearer " + JwtTestSupport.tokenExpirado(usuario.id(), email)), "get", "/auth/me", 401,
                "SESSAO_EXPIRADA");
    }

    // -------------------------------------------------------- procedimentos

    @Test
    void cadastrarProcedimento() throws Exception {
        String completo = """
                {"numProcedimento":9001,"nome":"Ressonância","codigoTuss":"40101010","categoria":"EXAMES_IMAGEM",
                 "especialidade":"NEUROLOGIA","tipoCobertura":"PLANO","quantidadeMaxima":2,"periodo":"ANO",
                 "exigeAutorizacaoPrevia":true,"exigeJustificativaMedica":false,"sexos":["f","M"],"idades":[30,18]}""";
        chamar(autenticado(json(post("/api/v1/procedimentos"), completo)), "post", "/procedimentos", 201, null);
        chamar(autenticado(json(post("/api/v1/procedimentos"),
                "{\"numProcedimento\":9002,\"nome\":\"Consulta\",\"categoria\":\"CONSULTAS\"}")), "post",
                "/procedimentos", 201, null);
        chamar(autenticado(json(post("/api/v1/procedimentos"), completo)), "post", "/procedimentos", 403,
                "PROCEDIMENTO_DUPLICADO");
        chamar(autenticado(json(post("/api/v1/procedimentos"), "{\"numProcedimento\":0}")), "post", "/procedimentos",
                400, "VALIDACAO_INVALIDA");
        chamar(autenticado(json(post("/api/v1/procedimentos"), "{numProcedimento")), "post", "/procedimentos", 400,
                "CORPO_INVALIDO");
    }

    @Test
    void listarEObterProcedimento() throws Exception {
        chamar(autenticado(json(post("/api/v1/procedimentos"),
                "{\"numProcedimento\":9003,\"nome\":\"Exame\",\"categoria\":\"EXAMES_LABORATORIAIS\"}")), "post",
                "/procedimentos", 201, null);

        chamar(autenticado(get("/api/v1/procedimentos")), "get", "/procedimentos", 200, null);
        chamar(autenticado(get("/api/v1/procedimentos").param("q", "90").param("size", "5").param("page", "0")
                .param("sort", "numProcedimento,desc")), "get", "/procedimentos", 200, null);
        chamar(autenticado(get("/api/v1/procedimentos").param("size", "0")), "get", "/procedimentos", 400, null);
        chamar(autenticado(get("/api/v1/procedimentos").param("page", "abc")), "get", "/procedimentos", 400,
                "PARAMETRO_INVALIDO");

        Object primeiro = ((List<?>) ((Map<?, ?>) chamar(autenticado(get("/api/v1/procedimentos").param("q", "1234")),
                "get", "/procedimentos", 200, null)).get("itens")).get(0);
        Object id = ((Map<?, ?>) primeiro).get("id");
        chamar(autenticado(get("/api/v1/procedimentos/" + id)), "get", "/procedimentos/{id}", 200, null);
        chamar(autenticado(get("/api/v1/procedimentos/999999")), "get", "/procedimentos/{id}", 404,
                "PROCEDIMENTO_NAO_ENCONTRADO");
        chamar(autenticado(get("/api/v1/procedimentos/abc")), "get", "/procedimentos/{id}", 400,
                "PARAMETRO_INVALIDO");
    }

    // ---------------------------------------------------------- autorizações

    @Test
    void solicitarEListarAutorizacoes() throws Exception {
        chamar(autenticado(json(post("/api/v1/autorizacoes"),
                "{\"numProcedimento\":4567,\"idade\":20,\"sexo\":\"F\"}")), "post", "/autorizacoes", 201, null);
        chamar(autenticado(json(post("/api/v1/autorizacoes"),
                "{\"numProcedimento\":1234,\"idade\":30,\"sexo\":\"M\"}")), "post", "/autorizacoes", 403,
                "AUTORIZACAO_NEGADA");
        chamar(autenticado(json(post("/api/v1/autorizacoes"),
                "{\"numProcedimento\":55555,\"idade\":30,\"sexo\":\"M\"}")), "post", "/autorizacoes", 404,
                "PROCEDIMENTO_NAO_ENCONTRADO");
        chamar(autenticado(json(post("/api/v1/autorizacoes"),
                "{\"numProcedimento\":4567,\"idade\":20,\"sexo\":\"Masculino\"}")), "post", "/autorizacoes", 400,
                "VALIDACAO_INVALIDA");

        chamar(autenticado(get("/api/v1/autorizacoes")), "get", "/autorizacoes", 200, null);
        chamar(autenticado(get("/api/v1/autorizacoes").param("size", "0")), "get", "/autorizacoes", 400, null);
    }

    // ---------------------------------------------------------------- painel

    @Test
    void obterResumoPainel() throws Exception {
        chamar(autenticado(json(post("/api/v1/autorizacoes"),
                "{\"numProcedimento\":4567,\"idade\":20,\"sexo\":\"F\"}")), "post", "/autorizacoes", 201, null);
        chamar(autenticado(get("/api/v1/painel/resumo")), "get", "/painel/resumo", 200, null);
    }

    // -------------------------------------------------------------- cobertura

    @Test
    void todosOsCodesDoContratoTemCobertura() {
        Set<String> cobertos = new TreeSet<>(COBERTOS_AQUI);
        cobertos.addAll(COBERTOS_EM_OUTROS_TESTES);
        assertThat(contrato.codigosDeErro()).containsExactlyInAnyOrderElementsOf(cobertos);
    }
}
