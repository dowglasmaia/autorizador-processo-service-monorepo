package org.maia.autorizador.shared.docs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Documentação da API somente no perfil {@code local}: serve o contrato OpenAPI estático de {@code openapi/}
 * (sem gerar o contrato a partir do código) e uma página mínima com o Swagger UI. Fora do {@code local} o bean
 * não existe e as rotas respondem {@code 404}.
 */
@RestController
@Profile("local")
class DocumentacaoApiController {

    private static final Pattern PASTA = Pattern.compile("modules|common");
    private static final Pattern ARQUIVO = Pattern.compile("[a-z0-9-]+[.]yaml");

    private static final String PAGINA = """
            <!doctype html>
            <html lang="pt-BR">
            <head>
              <meta charset="utf-8">
              <title>Autorizador — documentação da API</title>
              <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5/swagger-ui.css">
            </head>
            <body>
              <div id="swagger-ui"></div>
              <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
              <script>
                window.ui = SwaggerUIBundle({
                  dom_id: '#swagger-ui',
                  urls: [
                    {name: 'auth', url: '/v3/api-docs/modules/auth.yaml'},
                    {name: 'procedimento', url: '/v3/api-docs/modules/procedimento.yaml'},
                    {name: 'autorizacao', url: '/v3/api-docs/modules/autorizacao.yaml'},
                    {name: 'painel', url: '/v3/api-docs/modules/painel.yaml'}
                  ]
                });
              </script>
            </body>
            </html>
            """;

    @GetMapping(value = "/swagger-ui.html", produces = MediaType.TEXT_HTML_VALUE)
    String pagina() {
        return PAGINA;
    }

    @GetMapping(value = "/v3/api-docs/{pasta}/{arquivo}", produces = "application/yaml")
    ResponseEntity<String> contrato(@PathVariable String pasta, @PathVariable String arquivo) throws IOException {
        if (!PASTA.matcher(pasta).matches() || !ARQUIVO.matcher(arquivo).matches()) {
            return ResponseEntity.notFound().build();
        }
        var recurso = new ClassPathResource("openapi/" + pasta + "/" + arquivo);
        if (!recurso.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new String(recurso.getContentAsByteArray(), StandardCharsets.UTF_8));
    }
}
