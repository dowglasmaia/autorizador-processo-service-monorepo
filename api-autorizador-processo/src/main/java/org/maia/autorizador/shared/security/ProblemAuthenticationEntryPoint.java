package org.maia.autorizador.shared.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Responde {@code 401 problem+json}: {@code SESSAO_EXPIRADA} quando o token está expirado e
 * {@code NAO_AUTENTICADO} nos demais casos (ausente, adulterado, {@code alg=none}, {@code iss} errado).
 */
@Component
class ProblemAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
            throws IOException {
        if (expirado(ex)) {
            ProblemaSegurancaWriter.escrever(request, response, 401, "Sessão expirada",
                    "Sua sessão expirou. Entre novamente.", "SESSAO_EXPIRADA", true);
        } else {
            ProblemaSegurancaWriter.escrever(request, response, 401, "Não autenticado",
                    "Autenticação necessária.", "NAO_AUTENTICADO", true);
        }
    }

    private static boolean expirado(AuthenticationException ex) {
        if (ex instanceof InvalidBearerTokenException) {
            for (Throwable causa = ex; causa != null; causa = causa.getCause()) {
                String mensagem = causa.getMessage();
                if (mensagem != null && mensagem.contains("expired")) {
                    return true;
                }
            }
        }
        return false;
    }
}
