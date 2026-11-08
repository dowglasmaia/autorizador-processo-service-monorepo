package org.maia.autorizador.shared.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/** Escreve {@code problem+json} no mesmo formato do {@code GlobalExceptionHandler} para falhas ocorridas em filtros. */
final class ProblemaSegurancaWriter {

    private ProblemaSegurancaWriter() {
    }

    static void escrever(HttpServletRequest request, HttpServletResponse response, int status, String titulo,
            String detalhe, String code, boolean bearer) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        if (bearer) {
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        }
        String corpo = "{\"type\":\"about:blank\",\"title\":\"" + escapar(titulo) + "\",\"status\":" + status
                + ",\"detail\":\"" + escapar(detalhe) + "\",\"instance\":\"" + escapar(request.getRequestURI())
                + "\",\"code\":\"" + code + "\",\"timestamp\":\"" + Instant.now() + "\"}";
        response.getWriter().write(corpo);
    }

    private static String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
