package org.maia.autorizador.procedimento.infrastructure.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.envers.Audited;
import org.hibernate.type.SqlTypes;
import org.maia.autorizador.procedimento.domain.model.Sexo;

/**
 * Mapeamento JPA das tabelas {@code procedimento}, {@code procedimento_sexo} e {@code procedimento_idade} (V1).
 * Auditada pelo Envers nas tabelas {@code *_audit} da V2 (RN10). As coleções são preguiçosas e carregadas em lote
 * ({@code @BatchSize}) para evitar N+1 na listagem.
 */
@Entity
@Audited
@Table(name = "procedimento")
class ProcedimentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_procedimento", nullable = false, updatable = false)
    private int numeroProcedimento;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @ElementCollection
    @CollectionTable(name = "procedimento_sexo", joinColumns = @JoinColumn(name = "procedimento_id"))
    @Column(name = "sexo", nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    @BatchSize(size = 50)
    private Set<Sexo> sexos = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "procedimento_idade", joinColumns = @JoinColumn(name = "procedimento_id"))
    @Column(name = "idade", nullable = false)
    @JdbcTypeCode(SqlTypes.SMALLINT)
    @BatchSize(size = 50)
    private Set<Integer> idades = new HashSet<>();

    protected ProcedimentoEntity() {
    }

    ProcedimentoEntity(int numeroProcedimento, Collection<Sexo> sexos, Collection<Integer> idades) {
        this.numeroProcedimento = numeroProcedimento;
        this.sexos.addAll(sexos);
        this.idades.addAll(idades);
        // O PostgreSQL guarda microssegundos; truncar mantém o valor devolvido igual ao gravado.
        this.criadoEm = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    Long getId() {
        return id;
    }

    int getNumeroProcedimento() {
        return numeroProcedimento;
    }

    Instant getCriadoEm() {
        return criadoEm;
    }

    Set<Sexo> getSexos() {
        return sexos;
    }

    Set<Integer> getIdades() {
        return idades;
    }
}
