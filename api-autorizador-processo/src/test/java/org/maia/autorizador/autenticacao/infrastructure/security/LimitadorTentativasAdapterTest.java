package org.maia.autorizador.autenticacao.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class LimitadorTentativasAdapterTest {

    /** Relógio controlável pelo teste. */
    private static final class RelogioMutavel extends Clock {
        private Instant agora = Instant.parse("2026-01-01T10:00:00Z");

        void avancar(Duration d) {
            agora = agora.plus(d);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return agora;
        }
    }

    private final RelogioMutavel relogio = new RelogioMutavel();
    private final LimitadorTentativasAdapter limitador = new LimitadorTentativasAdapter(relogio);

    @Test
    void bloqueiaSomenteDepoisDeCincoFalhas() {
        for (int i = 0; i < 5; i++) {
            assertThat(limitador.bloqueioRestante("k")).isEmpty();
            limitador.registrarFalha("k");
        }

        assertThat(limitador.bloqueioRestante("k")).contains(Duration.ofMinutes(15));
        assertThat(limitador.bloqueioRestante("outra")).isEmpty();
    }

    @Test
    void bloqueioExpiraQuandoAFalhaMaisAntigaSaiDaJanela() {
        for (int i = 0; i < 5; i++) {
            limitador.registrarFalha("k");
        }
        relogio.avancar(Duration.ofMinutes(10));
        assertThat(limitador.bloqueioRestante("k")).contains(Duration.ofMinutes(5));

        relogio.avancar(Duration.ofMinutes(5));
        assertThat(limitador.bloqueioRestante("k")).isEmpty();
    }

    @Test
    void limparLiberaAChave() {
        for (int i = 0; i < 5; i++) {
            limitador.registrarFalha("k");
        }
        limitador.limpar("k");

        assertThat(limitador.bloqueioRestante("k")).isEmpty();
    }
}
