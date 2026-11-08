package org.maia.autorizador.autenticacao.infrastructure.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.maia.autorizador.autenticacao.application.port.LimitadorTentativas;
import org.springframework.stereotype.Component;

/**
 * Limitador em memória: no máximo 5 falhas por chave numa janela de 15 minutos; a tentativa seguinte é bloqueada
 * até a falha mais antiga sair da janela. Instância única (limitador distribuído fica fora do escopo, PEN-12).
 */
@Component
class LimitadorTentativasAdapter implements LimitadorTentativas {

    static final int MAXIMO_FALHAS = 5;
    static final Duration JANELA = Duration.ofMinutes(15);

    private final Clock relogio;
    private final ConcurrentHashMap<String, Deque<Instant>> falhas = new ConcurrentHashMap<>();

    LimitadorTentativasAdapter(Clock relogio) {
        this.relogio = relogio;
    }

    @Override
    public Optional<Duration> bloqueioRestante(String chave) {
        Deque<Instant> fila = falhas.get(chave);
        if (fila == null) {
            return Optional.empty();
        }
        Instant agora = relogio.instant();
        synchronized (fila) {
            descartarExpiradas(fila, agora);
            if (fila.size() < MAXIMO_FALHAS) {
                return Optional.empty();
            }
            return Optional.of(Duration.between(agora, fila.peekFirst().plus(JANELA)));
        }
    }

    @Override
    public void registrarFalha(String chave) {
        Instant agora = relogio.instant();
        Deque<Instant> fila = falhas.computeIfAbsent(chave, k -> new ArrayDeque<>());
        synchronized (fila) {
            descartarExpiradas(fila, agora);
            fila.addLast(agora);
        }
    }

    @Override
    public void limpar(String chave) {
        falhas.remove(chave);
    }

    private static void descartarExpiradas(Deque<Instant> fila, Instant agora) {
        while (!fila.isEmpty() && !fila.peekFirst().plus(JANELA).isAfter(agora)) {
            fila.removeFirst();
        }
    }
}
