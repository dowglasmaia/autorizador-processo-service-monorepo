package org.maia.autorizador.shared.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/** Responde {@code 403 problem+json} com {@code ACESSO_NEGADO}. */
@Component
class ProblemAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
            throws IOException {
        ProblemaSegurancaWriter.escrever(request, response, 403, "Acesso negado",
                "Você não tem permissão para esta operação.", "ACESSO_NEGADO", false);
    }
}
