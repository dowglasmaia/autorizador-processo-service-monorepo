package org.maia.autorizador.shared.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

/** Emite JWT HS256 com {@code iss}, {@code sub}, {@code email}, {@code iat} e {@code exp} (= {@code iat} + TTL). */
@Component
public class EmissorJwt {

    /** Token emitido e sua validade em segundos. */
    public record TokenEmitido(String valor, long expiraEmSegundos) {
    }

    private final JwtEncoder encoder;
    private final Clock relogio;
    private final String emissor;
    private final Duration ttl;

    public EmissorJwt(JwtEncoder encoder, Clock relogio,
            @Value("${app.jwt.issuer:autorizador-processo}") String emissor,
            @Value("${app.jwt.ttl-minutos:60}") long ttlMinutos) {
        this.encoder = encoder;
        this.relogio = relogio;
        this.emissor = emissor;
        this.ttl = Duration.ofMinutes(ttlMinutos);
    }

    public TokenEmitido emitir(String sujeito, String email) {
        Instant agora = relogio.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(emissor)
                .subject(sujeito)
                .claim("email", email)
                .issuedAt(agora)
                .expiresAt(agora.plus(ttl))
                .build();
        String valor = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
        return new TokenEmitido(valor, ttl.toSeconds());
    }
}
