package org.maia.autorizador.support;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Emite JWT para os testes das demais etapas, assinado com o {@code JWT_SECRET} dos perfis de teste
 * ({@code application-test.yml}). Os métodos permitem também gerar tokens inválidos (expirado, emissor errado).
 */
public final class JwtTestSupport {

    /** Mesmo valor de {@code app.jwt.secret} em {@code application-test.yml}; só existe em testes. */
    public static final String SEGREDO_DE_TESTE = "segredo-somente-para-testes-0123456789-abcdef";
    public static final String EMISSOR = "autorizador-processo";

    private JwtTestSupport() {
    }

    /** Token válido por uma hora para o usuário de id informado. */
    public static String tokenValido(long usuarioId, String email) {
        return token(SEGREDO_DE_TESTE, EMISSOR, usuarioId, email, Instant.now(), Duration.ofHours(1));
    }

    /** Valor do cabeçalho {@code Authorization} com um token válido. */
    public static String cabecalhoBearer(long usuarioId, String email) {
        return "Bearer " + tokenValido(usuarioId, email);
    }

    public static String tokenExpirado(long usuarioId, String email) {
        return token(SEGREDO_DE_TESTE, EMISSOR, usuarioId, email, Instant.now().minus(Duration.ofHours(2)),
                Duration.ofHours(1));
    }

    public static String tokenComEmissorErrado(long usuarioId, String email) {
        return token(SEGREDO_DE_TESTE, "outro-emissor", usuarioId, email, Instant.now(), Duration.ofHours(1));
    }

    public static String tokenAssinadoComOutroSegredo(long usuarioId, String email) {
        return token("outro-segredo-com-mais-de-32-bytes-0123456789", EMISSOR, usuarioId, email, Instant.now(),
                Duration.ofHours(1));
    }

    /** Assinatura e emissor corretos, mas sem a claim {@code exp}: deve ser rejeitado (SEC-14). */
    public static String tokenSemExpiracao(long usuarioId, String email) {
        var chave = new SecretKeySpec(SEGREDO_DE_TESTE.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        var encoder = new NimbusJwtEncoder(new ImmutableSecret<>(chave));
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(EMISSOR).subject(String.valueOf(usuarioId))
                .claim("email", email).issuedAt(Instant.now()).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }

    public static String token(String segredo, String emissor, long usuarioId, String email, Instant emitidoEm,
            Duration validade) {
        var chave = new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        var encoder = new NimbusJwtEncoder(new ImmutableSecret<>(chave));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(emissor)
                .subject(String.valueOf(usuarioId))
                .claim("email", email)
                .issuedAt(emitidoEm)
                .expiresAt(emitidoEm.plus(validade))
                .build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }
}
