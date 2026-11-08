package org.maia.autorizador.shared.security;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cadeia de segurança stateless (SEC-13..SEC-20): sem sessão e sem CSRF (não há cookies), CORS integrado à
 * configuração MVC da {@code CorsConfig}, JWT Bearer como único meio de autenticação. Público somente o login,
 * o health e, no perfil {@code local}, a documentação; todo o resto exige autenticação.
 */
@Configuration
public class SecurityConfig {

    private static final String[] DOCUMENTACAO = {"/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"};

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, Environment ambiente,
            ProblemAuthenticationEntryPoint entryPoint, ProblemAccessDeniedHandler negado) throws Exception {
        boolean perfilLocal = ambiente.acceptsProfiles(Profiles.of("local"));
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(a -> {
                    a.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    a.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll();
                    a.requestMatchers("/actuator/health", "/actuator/health/**").permitAll();
                    if (perfilLocal) {
                        a.requestMatchers(DOCUMENTACAO).permitAll();
                    }
                    a.anyRequest().authenticated();
                })
                .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint).accessDeniedHandler(negado))
                .oauth2ResourceServer(o -> o.jwt(withDefaults())
                        .authenticationEntryPoint(entryPoint).accessDeniedHandler(negado));
        return http.build();
    }
}
