package org.maia.autorizador.procedimento.api;

import java.util.Locale;
import org.maia.autorizador.generated.procedimento.model.Sexo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.module.SimpleModule;

/**
 * O contrato aceita {@code sexos} em minúsculas na entrada (DIV-02), mas o enum gerado compara o texto exato.
 * Este módulo troca a leitura do enum gerado por uma sem diferença de caixa; valor inválido continua virando
 * {@code 400 CORPO_INVALIDO}.
 */
@Configuration(proxyBeanMethods = false)
class SexoJacksonConfig {

    @Bean
    JacksonModule sexoSemDiferencaDeCaixaModule() {
        var modulo = new SimpleModule("sexo-sem-diferenca-de-caixa");
        modulo.addDeserializer(Sexo.class, new SexoDeserializer());
        return modulo;
    }

    static final class SexoDeserializer extends ValueDeserializer<Sexo> {

        @Override
        public Sexo deserialize(JsonParser parser, DeserializationContext contexto) {
            String texto = parser.getValueAsString();
            String normalizado = texto == null ? "" : texto.strip().toUpperCase(Locale.ROOT);
            return switch (normalizado) {
                case "M" -> Sexo.M;
                case "F" -> Sexo.F;
                default -> (Sexo) contexto.handleWeirdStringValue(Sexo.class, texto, "use M ou F");
            };
        }
    }
}
