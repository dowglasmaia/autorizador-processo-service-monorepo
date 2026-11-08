package org.maia.autorizador.shared.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.maia.autorizador.shared.error.BusinessRuleException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Controller descartável, só de teste: dispara cada tipo de erro tratado pelo {@code GlobalExceptionHandler}. */
@RestController
@Validated
@RequestMapping("/api/v1/teste-erros")
class ErroTesteController {

    record Corpo(@NotBlank(message = "não pode ser vazio") String nome) {
    }

    @PostMapping("/validacao")
    String validacao(@Valid @RequestBody Corpo corpo) {
        return corpo.nome();
    }

    @GetMapping("/parametro-tipo")
    String parametroTipo(@RequestParam int numero) {
        return String.valueOf(numero);
    }

    @GetMapping("/parametro-ausente")
    String parametroAusente(@RequestParam String obrigatorio) {
        return obrigatorio;
    }

    @GetMapping("/restricao")
    String restricao(@RequestParam @Min(value = 1, message = "deve ser maior que zero") int pagina) {
        return String.valueOf(pagina);
    }

    @GetMapping("/negocio-proibido")
    String proibido() {
        throw new BusinessRuleException("OPERACAO_NAO_PERMITIDA", BusinessRuleException.Categoria.OPERACAO_NAO_PERMITIDA,
                "Operação não permitida.");
    }

    @GetMapping("/negocio-inexistente")
    String inexistente() {
        throw new BusinessRuleException("PROCEDIMENTO_NAO_ENCONTRADO", BusinessRuleException.Categoria.NAO_ENCONTRADO,
                "Procedimento não encontrado.");
    }

    @GetMapping("/inesperado")
    String inesperado() {
        throw new IllegalStateException("select * from tabela_secreta where senha = 1");
    }
}
