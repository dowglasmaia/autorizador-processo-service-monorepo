package org.maia.autorizador.autenticacao.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.maia.autorizador.autenticacao.application.port.HashSenha;
import org.maia.autorizador.autenticacao.application.port.UsuarioRepository;
import org.maia.autorizador.autenticacao.domain.model.Usuario;
import org.maia.autorizador.support.IntegrationTestBase;
import org.maia.autorizador.support.JwtTestSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Login, proteção por JWT e limitador, ponta a ponta sobre PostgreSQL real. */
@AutoConfigureMockMvc
class AuthApiTest extends IntegrationTestBase {

    private static final String SENHA = "senha-de-teste-123";

    @Autowired
    MockMvc mvc;

    @Autowired
    UsuarioRepository usuarios;

    @Autowired
    HashSenha hashSenha;

    private Usuario usuario;
    private String email;

    @BeforeEach
    void criarUsuario() {
        // E-mail e IP únicos por teste: o limitador em memória vive no contexto compartilhado.
        email = "u" + UUID.randomUUID().toString().substring(0, 8) + "@ex.com";
        usuario = usuarios.salvar(Usuario.novo(email, hashSenha.gerar(SENHA)));
    }

    private ResultActions login(String emailLogin, String senha, String ip) throws Exception {
        String corpo = "{\"email\":\"" + emailLogin + "\",\"senha\":\"" + senha + "\"}";
        return mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(corpo)
                .with(r -> {
                    r.setRemoteAddr(ip);
                    return r;
                }));
    }

    private ResultActions me(String cabecalho) throws Exception {
        var req = get("/api/v1/auth/me");
        if (cabecalho != null) {
            req = req.header(HttpHeaders.AUTHORIZATION, cabecalho);
        }
        return mvc.perform(req);
    }

    private void naoAutenticado(ResultActions r, String code) throws Exception {
        r.andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void loginValidoRetorna200ComJwtDeValidadeConfigurada() throws Exception {
        String json = login(email.toUpperCase(), SENHA, "10.1.0.1").andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiraEm").value(3600))
                .andExpect(jsonPath("$.usuario.email").value(email))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        String token = com.jayway.jsonpath.JsonPath.read(json, "$.accessToken");
        var claims = SignedJWT.parse(token).getJWTClaimsSet();
        assertThat(claims.getIssuer()).isEqualTo(JwtTestSupport.EMISSOR);
        assertThat(claims.getSubject()).isEqualTo(String.valueOf(usuario.id()));
        assertThat(Duration.between(claims.getIssueTime().toInstant(), claims.getExpirationTime().toInstant()))
                .isEqualTo(Duration.ofMinutes(60));

        me("Bearer " + token).andExpect(status().isOk()).andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void credenciaisInvalidasDaoMesmaRespostaParaSenhaErradaEUsuarioInexistente() throws Exception {
        String esperado = "E-mail ou senha inválidos.";

        login(email, "errada", "10.1.0.2").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CREDENCIAIS_INVALIDAS"))
                .andExpect(jsonPath("$.detail").value(esperado));
        login("nao-existe@ex.com", "qualquer", "10.1.0.2").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CREDENCIAIS_INVALIDAS"))
                .andExpect(jsonPath("$.detail").value(esperado));
    }

    @Test
    void usuarioInativoNaoAutentica() throws Exception {
        String inativo = "inativo" + UUID.randomUUID().toString().substring(0, 6) + "@ex.com";
        usuarios.salvar(new Usuario(null, inativo, hashSenha.gerar(SENHA), false));

        login(inativo, SENHA, "10.1.0.3").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CREDENCIAIS_INVALIDAS"));
    }

    @Test
    void sextaFalhaDoMesmoEmailEIpRetorna429ComRetryAfter() throws Exception {
        for (int i = 0; i < 5; i++) {
            login(email, "errada", "10.1.0.4").andExpect(status().isUnauthorized());
        }

        login(email, SENHA, "10.1.0.4").andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("MUITAS_TENTATIVAS"))
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER));
        // Outro IP não é afetado.
        login(email, SENHA, "10.1.0.5").andExpect(status().isOk());
    }

    @Test
    void rotaProtegidaSemTokenRetorna401NaoAutenticado() throws Exception {
        naoAutenticado(me(null), "NAO_AUTENTICADO");
    }

    @Test
    void tokenValidoEhAceito() throws Exception {
        me(JwtTestSupport.cabecalhoBearer(usuario.id(), email)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(usuario.id()));
    }

    @Test
    void tokenExpiradoRetornaSessaoExpirada() throws Exception {
        naoAutenticado(me("Bearer " + JwtTestSupport.tokenExpirado(usuario.id(), email)), "SESSAO_EXPIRADA");
    }

    @Test
    void tokenAdulteradoRetorna401() throws Exception {
        String valido = JwtTestSupport.tokenValido(usuario.id(), email);
        String[] partes = valido.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(partes[1]), StandardCharsets.UTF_8)
                .replace("\"sub\":\"" + usuario.id() + "\"", "\"sub\":\"999999\"");
        String adulterado = partes[0] + "."
                + Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8))
                + "." + partes[2];
        assertThat(adulterado).isNotEqualTo(valido);

        naoAutenticado(me("Bearer " + adulterado), "NAO_AUTENTICADO");
        naoAutenticado(me("Bearer " + JwtTestSupport.tokenAssinadoComOutroSegredo(usuario.id(), email)),
                "NAO_AUTENTICADO");
    }

    @Test
    void tokenAlgNoneEhRejeitado() throws Exception {
        var base64 = Base64.getUrlEncoder().withoutPadding();
        String cabecalho = base64.encodeToString("{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        long exp = Instant.now().plus(Duration.ofHours(1)).getEpochSecond();
        String payload = base64.encodeToString(("{\"iss\":\"" + JwtTestSupport.EMISSOR + "\",\"sub\":\""
                + usuario.id() + "\",\"exp\":" + exp + "}").getBytes(StandardCharsets.UTF_8));

        naoAutenticado(me("Bearer " + cabecalho + "." + payload + "."), "NAO_AUTENTICADO");
    }

    @Test
    void tokenComEmissorErradoRetorna401() throws Exception {
        naoAutenticado(me("Bearer " + JwtTestSupport.tokenComEmissorErrado(usuario.id(), email)),
                "NAO_AUTENTICADO");
    }

    @Test
    void tokenSemExpiracaoRetorna401() throws Exception {
        naoAutenticado(me("Bearer " + JwtTestSupport.tokenSemExpiracao(usuario.id(), email)), "NAO_AUTENTICADO");
    }

    @Test
    void respostasDaApiNaoSaoArmazenadasEmCache() throws Exception {
        String token = login(email, SENHA, "10.9.9." + (int) (Math.random() * 200 + 1))
                .andReturn().getResponse().getContentAsString();
        String jwt = token.replaceAll(".*\"(?:accessToken|token)\"\s*:\s*\"([^\"]+)\".*", "$1");
        me("Bearer " + jwt).andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, org.hamcrest.Matchers.containsString("no-store")));
    }
}
