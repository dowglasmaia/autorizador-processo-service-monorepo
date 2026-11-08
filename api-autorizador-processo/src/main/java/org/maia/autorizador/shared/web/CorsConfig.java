package org.maia.autorizador.shared.web;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS por allowlist (SEC-05). {@code CORS_ALLOWED_ORIGINS} é uma lista separada por vírgula; o padrão é vazio
 * (nenhuma origem liberada) e o curinga {@code *} é sempre ignorado.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] origensPermitidas;

    public CorsConfig(@Value("${app.cors.allowed-origins:}") String origens) {
        this.origensPermitidas = Arrays.stream(origens.split(","))
                .map(String::trim)
                .filter(o -> !o.isEmpty() && !o.equals("*"))
                .toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (origensPermitidas.length == 0) {
            return;
        }
        registry.addMapping("/api/**")
                .allowedOrigins(origensPermitidas)
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type")
                .maxAge(3600);
    }
}
