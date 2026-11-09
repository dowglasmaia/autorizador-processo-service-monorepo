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
import org.maia.autorizador.procedimento.domain.model.Categoria;
import org.maia.autorizador.procedimento.domain.model.DadosCatalogo;
import org.maia.autorizador.procedimento.domain.model.Especialidade;
import org.maia.autorizador.procedimento.domain.model.FaixaEtaria;
import org.maia.autorizador.procedimento.domain.model.Periodo;
import org.maia.autorizador.procedimento.domain.model.Sexo;
import org.maia.autorizador.procedimento.domain.model.TipoCobertura;

/**
 * Mapeamento JPA das tabelas {@code procedimento}, {@code procedimento_sexo} e {@code procedimento_faixa_etaria} (V1, V5).
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

    @Column(name = "nome", length = 200)
    private String nome;

    @Column(name = "codigo_tuss", length = 30)
    private String codigoTuss;

    @Column(name = "categoria", length = 30)
    @Enumerated(EnumType.STRING)
    private Categoria categoria;

    @Column(name = "especialidade", length = 30)
    @Enumerated(EnumType.STRING)
    private Especialidade especialidade;

    @Column(name = "tipo_cobertura", length = 20)
    @Enumerated(EnumType.STRING)
    private TipoCobertura tipoCobertura;

    @Column(name = "quantidade_maxima")
    private Integer quantidadeMaxima;

    @Column(name = "periodo", length = 10)
    @Enumerated(EnumType.STRING)
    private Periodo periodo;

    @Column(name = "exige_autorizacao_previa")
    private Boolean exigeAutorizacaoPrevia;

    @Column(name = "exige_justificativa_medica")
    private Boolean exigeJustificativaMedica;

    @ElementCollection
    @CollectionTable(name = "procedimento_sexo", joinColumns = @JoinColumn(name = "procedimento_id"))
    @Column(name = "sexo", nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    @BatchSize(size = 200)
    private Set<Sexo> sexos = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "procedimento_faixa_etaria", joinColumns = @JoinColumn(name = "procedimento_id"))
    @Column(name = "faixa", nullable = false, length = 12)
    @Enumerated(EnumType.STRING)
    @BatchSize(size = 200)
    private Set<FaixaEtaria> faixasEtarias = new HashSet<>();

    protected ProcedimentoEntity() {
    }

    ProcedimentoEntity(int numeroProcedimento, Collection<Sexo> sexos, Collection<FaixaEtaria> faixasEtarias,
            DadosCatalogo catalogo) {
        this.numeroProcedimento = numeroProcedimento;
        this.nome = catalogo.nome();
        this.codigoTuss = catalogo.codigoTuss();
        this.categoria = catalogo.categoria();
        this.especialidade = catalogo.especialidade();
        this.tipoCobertura = catalogo.tipoCobertura();
        this.quantidadeMaxima = catalogo.quantidadeMaxima();
        this.periodo = catalogo.periodo();
        this.exigeAutorizacaoPrevia = catalogo.exigeAutorizacaoPrevia();
        this.exigeJustificativaMedica = catalogo.exigeJustificativaMedica();
        this.sexos.addAll(sexos);
        this.faixasEtarias.addAll(faixasEtarias);
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

    /** Dados de catálogo; campos nulos nas linhas anteriores à V4. */
    DadosCatalogo catalogo() {
        return new DadosCatalogo(nome, codigoTuss, categoria, especialidade, tipoCobertura, quantidadeMaxima,
                periodo, exigeAutorizacaoPrevia, exigeJustificativaMedica);
    }

    Set<Sexo> getSexos() {
        return sexos;
    }

    Set<FaixaEtaria> getFaixasEtarias() {
        return faixasEtarias;
    }
}
