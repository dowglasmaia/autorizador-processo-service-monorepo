package org.maia.autorizador.autorizacao.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.hibernate.envers.Audited;

/**
 * Mapeamento JPA da tabela {@code autorizacao} (V1), auditada nas tabelas {@code *_audit} da V2 (RN10). Guarda só o
 * {@code procedimento_id} (FK); o procedimento é carregado em lote pelo caso de uso. Idade e sexo não são gravados.
 */
@Entity
@Audited
@Table(name = "autorizacao")
class AutorizacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "procedimento_id", nullable = false, updatable = false)
    private long procedimentoId;

    @Column(name = "permitido", nullable = false, updatable = false)
    private boolean permitido;

    @Column(name = "criada_em", updatable = false)
    private Instant criadaEm;

    protected AutorizacaoEntity() {
    }

    AutorizacaoEntity(long procedimentoId, boolean permitido) {
        this.procedimentoId = procedimentoId;
        this.permitido = permitido;
        // O PostgreSQL guarda microssegundos; truncar mantém o valor devolvido igual ao gravado.
        this.criadaEm = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    Long getId() {
        return id;
    }

    long getProcedimentoId() {
        return procedimentoId;
    }

    boolean isPermitido() {
        return permitido;
    }

    Instant getCriadaEm() {
        return criadaEm;
    }
}
