package org.maia.autorizador.shared.time;

import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Relógio injetável: permite fixar o tempo nos testes (emissão de token, limitador de tentativas). */
@Configuration
public class ClockConfig {

    @Bean
    @ConditionalOnMissingBean
    Clock relogio() {
        return Clock.systemUTC();
    }
}
