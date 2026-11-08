package org.maia.autorizador.shared.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.maia.autorizador.shared.error.BusinessRuleException;
import org.maia.autorizador.shared.error.CampoInvalidoException;
import org.maia.autorizador.shared.error.LimiteExcedidoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Único ponto de tradução de exceções em respostas {@code application/problem+json} (RFC 9457),
 * alinhadas ao esquema {@code ProblemDetail} de {@code openapi/common/responses.yaml}.
 * Nunca expõe stack trace, nome de classe ou SQL no corpo; o {@code traceId} fica só no log.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String TITULO_REQUISICAO_INVALIDA = "Requisição inválida";

    @ExceptionHandler(BusinessRuleException.class)
    ResponseEntity<ProblemDetail> negocio(BusinessRuleException ex, HttpServletRequest request) {
        HttpStatus status;
        String titulo;
        switch (ex.getCategoria()) {
            case NAO_ENCONTRADO -> {
                status = HttpStatus.NOT_FOUND;
                titulo = "Não encontrado";
            }
            case NAO_AUTENTICADO -> {
                status = HttpStatus.UNAUTHORIZED;
                titulo = "Não autenticado";
            }
            case LIMITE_EXCEDIDO -> {
                status = HttpStatus.TOO_MANY_REQUESTS;
                titulo = "Muitas tentativas";
            }
            default -> {
                status = HttpStatus.FORBIDDEN;
                titulo = "Operação não permitida";
            }
        }
        ResponseEntity<ProblemDetail> base = resposta(status, titulo, ex.getMessage(), ex.getCode(), request,
                List.of());
        ResponseEntity.BodyBuilder construtor = ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON);
        if (status == HttpStatus.UNAUTHORIZED) {
            construtor.header(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        }
        if (ex instanceof LimiteExcedidoException limite) {
            construtor.header(HttpHeaders.RETRY_AFTER, Long.toString(limite.getRetryAfterSegundos()));
        }
        return construtor.body(base.getBody());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> validacao(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<Map<String, String>> erros = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(erro -> {
            String campo = erro instanceof FieldError fe ? fe.getField() : erro.getObjectName();
            erros.add(violacao(campo, erro.getDefaultMessage()));
        });
        return validacaoInvalida(request, erros);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ProblemDetail> validacaoDeMetodo(HandlerMethodValidationException ex, HttpServletRequest request) {
        List<Map<String, String>> erros = new ArrayList<>();
        ex.getParameterValidationResults().forEach(resultado -> {
            String campo = resultado.getMethodParameter().getParameterName();
            for (MessageSourceResolvable erro : resultado.getResolvableErrors()) {
                erros.add(violacao(campo, erro.getDefaultMessage()));
            }
        });
        return validacaoInvalida(request, erros);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ProblemDetail> validacaoDeRestricao(ConstraintViolationException ex, HttpServletRequest request) {
        List<Map<String, String>> erros = new ArrayList<>();
        ex.getConstraintViolations().forEach(v -> {
            String caminho = v.getPropertyPath().toString();
            erros.add(violacao(caminho.substring(caminho.lastIndexOf('.') + 1), v.getMessage()));
        });
        return validacaoInvalida(request, erros);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ProblemDetail> corpoInvalido(HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, TITULO_REQUISICAO_INVALIDA, "Corpo da requisição inválido.",
                "CORPO_INVALIDO", request, List.of());
    }

    /**
     * Invariante de domínio ou parâmetro fora do permitido que o contrato não barrou (ex.: {@code sort} inválido):
     * é entrada inválida do cliente, não falha interna.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ProblemDetail> argumentoInvalido(IllegalArgumentException ex, HttpServletRequest request) {
        return validacaoInvalida(request, List.of(violacao("requisicao", ex.getMessage())));
    }

    @ExceptionHandler(CampoInvalidoException.class)
    ResponseEntity<ProblemDetail> campoInvalido(CampoInvalidoException ex, HttpServletRequest request) {
        return validacaoInvalida(request, List.of(violacao(ex.getCampo(), ex.getMessage())));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ProblemDetail> parametroComTipoInvalido(MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        return parametroInvalido(ex.getName(), request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ProblemDetail> parametroAusente(MissingServletRequestParameterException ex,
            HttpServletRequest request) {
        return parametroInvalido(ex.getParameterName(), request);
    }

    /** Rota inexistente: 404 genérico, sem {@code code} de negócio (o enum do contrato não ganha valor novo). */
    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ProblemDetail> rotaInexistente(HttpServletRequest request) {
        return resposta(HttpStatus.NOT_FOUND, "Não encontrado", "Recurso não encontrado.", null, request, List.of());
    }

    /** Método HTTP não previsto na rota: 405 com {@code Allow}, sem {@code code} (o enum do contrato não ganha valor). */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ProblemDetail> metodoNaoPermitido(HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {
        ResponseEntity<ProblemDetail> base = resposta(HttpStatus.METHOD_NOT_ALLOWED, "Método não permitido",
                "Método não permitido para este recurso.", null, request, List.of());
        var permitidos = ex.getSupportedHttpMethods();
        if (permitidos == null || permitidos.isEmpty()) {
            return base;
        }
        return ResponseEntity.status(base.getStatusCode()).allow(permitidos.toArray(new org.springframework.http.HttpMethod[0]))
                .contentType(MediaType.APPLICATION_PROBLEM_JSON).body(base.getBody());
    }

    /** Content-Type não aceito pela rota: 415 genérico. */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ProblemDetail> tipoDeMidiaNaoSuportado(HttpServletRequest request) {
        return resposta(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de mídia não suportado",
                "Use Content-Type application/json.", null, request, List.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> inesperada(Exception ex, HttpServletRequest request) {
        log.error("Falha inesperada em {} {} (traceId={})", request.getMethod(), request.getRequestURI(),
                MDC.get("traceId"), ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Erro interno. Tente novamente mais tarde.", "ERRO_INTERNO", request, List.of());
    }

    private ResponseEntity<ProblemDetail> parametroInvalido(String nome, HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, TITULO_REQUISICAO_INVALIDA, "Parâmetro inválido: " + nome + ".",
                "PARAMETRO_INVALIDO", request, List.of());
    }

    private ResponseEntity<ProblemDetail> validacaoInvalida(HttpServletRequest request,
            List<Map<String, String>> erros) {
        return resposta(HttpStatus.BAD_REQUEST, TITULO_REQUISICAO_INVALIDA, "Dados inválidos.",
                "VALIDACAO_INVALIDA", request, erros);
    }

    private static Map<String, String> violacao(String campo, String mensagem) {
        return Map.of("campo", String.valueOf(campo), "mensagem", mensagem == null ? "inválido" : mensagem);
    }

    private static ResponseEntity<ProblemDetail> resposta(HttpStatus status, String titulo, String detalhe,
            String code, HttpServletRequest request, List<Map<String, String>> erros) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setType(URI.create("about:blank"));
        problema.setTitle(titulo);
        problema.setInstance(URI.create(request.getRequestURI()));
        if (code != null) {
            problema.setProperty("code", code);
        }
        problema.setProperty("timestamp", Instant.now().toString());
        if (!erros.isEmpty()) {
            problema.setProperty("errors", erros);
        }
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problema);
    }
}
