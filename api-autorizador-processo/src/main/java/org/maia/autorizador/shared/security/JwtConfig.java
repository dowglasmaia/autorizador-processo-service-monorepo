package org.maia.autorizador.shared.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * JWT HS256 (ADR-16). O segredo vem de {@code JWT_SECRET}, sem valor padrão: ausente ou com menos de 32 bytes
 * a aplicação não inicia. O decodificador valida assinatura, algoritmo (apenas HS256, rejeita {@code none}),
 * {@code exp} e {@code iss}.
 */
@Configuration
public class JwtConfig {

    static final int TAMANHO_MINIMO_SEGREDO_BYTES = 32;

    private final SecretKey chave;
    private final String emissor;

    public JwtConfig(
            @Value("${app.jwt.secret:}") String segredo,
            @Value("${app.jwt.issuer:autorizador-processo}") String emissor) {
        byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < TAMANHO_MINIMO_SEGREDO_BYTES) {
            throw new IllegalStateException("JWT_SECRET ausente ou com menos de " + TAMANHO_MINIMO_SEGREDO_BYTES
                    + " bytes: defina a variável de ambiente JWT_SECRET.");
        }
        this.chave = new SecretKeySpec(bytes, "HmacSHA256");
        this.emissor = emissor;
    }

    @Bean
    JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(chave));
    }

    @Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(chave).macAlgorithm(MacAlgorithm.HS256).build();
        OAuth2TokenValidator<Jwt> validador = new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(emissor));
        decoder.setJwtValidator(validador);
        return decoder;
    }
}
