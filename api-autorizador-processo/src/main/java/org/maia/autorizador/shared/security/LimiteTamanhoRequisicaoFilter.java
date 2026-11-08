package org.maia.autorizador.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Limite de tamanho do corpo (SEC-03, SPEC-41). O Tomcat não limita corpos JSON; este filtro recusa, antes da
 * autenticação e sem ler o corpo, requisições cujo {@code Content-Length} excede {@code app.http.max-body-bytes}
 * ({@code HTTP_MAX_BODY_BYTES}, padrão 64 KiB) com {@code 413}, e corpos sem tamanho declarado
 * ({@code Transfer-Encoding: chunked}) com {@code 411}. O limite de cabeçalhos é do Tomcat
 * ({@code server.max-http-request-header-size}, {@code HTTP_MAX_HEADER_BYTES}). O {@code code} reutiliza
 * {@code CORPO_INVALIDO} para não ampliar o enum do contrato.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class LimiteTamanhoRequisicaoFilter extends OncePerRequestFilter {

    private final long maximoBytes;

    public LimiteTamanhoRequisicaoFilter(@Value("${app.http.max-body-bytes:65536}") long maximoBytes) {
        this.maximoBytes = maximoBytes;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long tamanho = request.getContentLengthLong();
        if (tamanho > maximoBytes) {
            ProblemaSegurancaWriter.escrever(request, response, 413, "Corpo grande demais",
                    "O corpo da requisição excede o limite de " + maximoBytes + " bytes.", "CORPO_INVALIDO", false);
            return;
        }
        if (tamanho < 0 && request.getHeader("Transfer-Encoding") != null) {
            ProblemaSegurancaWriter.escrever(request, response, 411, "Tamanho do corpo obrigatório",
                    "Informe o cabeçalho Content-Length.", "CORPO_INVALIDO", false);
            return;
        }
        chain.doFilter(request, response);
    }
}
