package org.maia.autorizador.autorizacao.domain.model;

import java.time.Instant;
import java.util.Objects;
import org.maia.autorizador.procedimento.domain.model.Procedimento;

/**
 * Autorização concedida para um procedimento. Só existem autorizações concedidas (RN8): a negativa não é gravada,
 * portanto {@code permitido} é sempre verdadeiro. Idade e sexo do paciente não fazem parte do registro (PEN-01).
 * Construída por {@link #conceder} ou {@link #restore}; sem setters.
 */
public final class Autorizacao {

    private final Long id;
    private final long procedimentoId;
    private final boolean permitido;
    private final Instant criadaEm;

    private Autorizacao(Long id, long procedimentoId, boolean permitido, Instant criadaEm) {
        this.id = id;
        this.procedimentoId = procedimentoId;
        this.permitido = permitido;
        this.criadaEm = criadaEm;
    }

    /** Nova autorização para um procedimento já gravado; identificador e data são atribuídos na persistência. */
    public static Autorizacao conceder(Procedimento procedimento) {
        Objects.requireNonNull(procedimento, "procedimento");
        if (procedimento.id() == null) {
            throw new IllegalArgumentException("O procedimento precisa estar gravado para ser autorizado.");
        }
        return new Autorizacao(null, procedimento.id(), true, null);
    }

    /** Reidrata uma autorização gravada ({@code criadaEm} pode ser nulo). */
    public static Autorizacao restore(long id, long procedimentoId, boolean permitido, Instant criadaEm) {
        return new Autorizacao(id, procedimentoId, permitido, criadaEm);
    }

    /** Nulo enquanto não foi gravada. */
    public Long id() {
        return id;
    }

    public long procedimentoId() {
        return procedimentoId;
    }

    public boolean permitido() {
        return permitido;
    }

    /** Nulo enquanto não foi gravada (e em linhas sem data). */
    public Instant criadaEm() {
        return criadaEm;
    }
}
