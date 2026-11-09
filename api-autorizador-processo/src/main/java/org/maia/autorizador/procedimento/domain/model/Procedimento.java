package org.maia.autorizador.procedimento.domain.model;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Procedimento autorizável: o número é imutável e as listas de sexos e faixas etárias aceitos podem ser vazias (RN9).
 * Construído por {@link #create} (aplica as invariantes) ou {@link #restore} (reidrata o já gravado); sem setters.
 */
public final class Procedimento {

    private final Long id;
    private final int numProcedimento;
    private final Set<Sexo> sexos;
    private final Set<FaixaEtaria> faixasEtarias;
    private final Instant criadoEm;
    private final DadosCatalogo catalogo;

    private Procedimento(Long id, int numProcedimento, Set<Sexo> sexos, Set<FaixaEtaria> faixasEtarias,
            Instant criadoEm, DadosCatalogo catalogo) {
        this.id = id;
        this.numProcedimento = numProcedimento;
        this.sexos = Collections.unmodifiableSet(sexos);
        this.faixasEtarias = Collections.unmodifiableSet(faixasEtarias);
        this.criadoEm = criadoEm;
        this.catalogo = catalogo;
    }

    /** Novo procedimento, ainda sem identificador nem data (atribuídos na persistência). */
    public static Procedimento create(int numProcedimento, Collection<Sexo> sexos, Collection<FaixaEtaria> faixasEtarias) {
        return create(numProcedimento, sexos, faixasEtarias, DadosCatalogo.VAZIO);
    }

    /** Novo procedimento com os dados de catálogo (NEW-01), que não influenciam {@link #avaliar}. */
    public static Procedimento create(int numProcedimento, Collection<Sexo> sexos, Collection<FaixaEtaria> faixasEtarias,
            DadosCatalogo catalogo) {
        Objects.requireNonNull(catalogo, "catalogo");
        if (numProcedimento < 1) {
            throw new IllegalArgumentException("O número do procedimento deve ser maior ou igual a 1.");
        }
        Objects.requireNonNull(sexos, "sexos");
        Objects.requireNonNull(faixasEtarias, "faixasEtarias");
        Set<Sexo> sexosOrdenados = EnumSet.noneOf(Sexo.class);
        for (Sexo sexo : sexos) {
            if (!sexosOrdenados.add(Objects.requireNonNull(sexo, "sexo"))) {
                throw new IllegalArgumentException("Sexos duplicados.");
            }
        }
        Set<FaixaEtaria> faixasOrdenadas = EnumSet.noneOf(FaixaEtaria.class);
        for (FaixaEtaria faixa : faixasEtarias) {
            if (!faixasOrdenadas.add(Objects.requireNonNull(faixa, "faixa"))) {
                throw new IllegalArgumentException("Faixas etárias duplicadas.");
            }
        }
        return new Procedimento(null, numProcedimento, sexosOrdenados, faixasOrdenadas, null, catalogo);
    }

    /** Reidrata um procedimento gravado (sem reaplicar a criação). */
    public static Procedimento restore(long id, int numProcedimento, Collection<Sexo> sexos,
            Collection<FaixaEtaria> faixasEtarias, Instant criadoEm) {
        return restore(id, numProcedimento, sexos, faixasEtarias, criadoEm, DadosCatalogo.VAZIO);
    }

    /** Reidrata um procedimento gravado com seus dados de catálogo (nulos nas linhas anteriores). */
    public static Procedimento restore(long id, int numProcedimento, Collection<Sexo> sexos,
            Collection<FaixaEtaria> faixasEtarias, Instant criadoEm, DadosCatalogo catalogo) {
        Set<Sexo> sexosOrdenados = EnumSet.noneOf(Sexo.class);
        sexosOrdenados.addAll(sexos);
        Set<FaixaEtaria> faixasOrdenadas = EnumSet.noneOf(FaixaEtaria.class);
        faixasOrdenadas.addAll(faixasEtarias);
        return new Procedimento(id, numProcedimento, sexosOrdenados, faixasOrdenadas, criadoEm,
                catalogo == null ? DadosCatalogo.VAZIO : catalogo);
    }

    /** RN5–RN7: autoriza quando o sexo está entre os aceitos E a faixa etária da idade está entre as aceitas. */
    public boolean avaliar(int idade, Sexo sexo) {
        Objects.requireNonNull(sexo, "sexo");
        return faixasEtarias.contains(FaixaEtaria.de(idade)) && sexos.contains(sexo);
    }

    /** Aceita o sexo em texto, sem diferença de caixa; valor inválido lança {@link IllegalArgumentException}. */
    public boolean avaliar(int idade, String sexo) {
        return avaliar(idade, Sexo.parse(sexo));
    }

    /** Nulo enquanto o procedimento não foi gravado. */
    public Long id() {
        return id;
    }

    public int numProcedimento() {
        return numProcedimento;
    }

    /** Sexos aceitos, na ordem M, F; somente leitura. */
    public Set<Sexo> sexos() {
        return sexos;
    }

    /** Faixas etárias aceitas, da mais nova para a mais velha; somente leitura. */
    public Set<FaixaEtaria> faixasEtarias() {
        return faixasEtarias;
    }

    /** Nulo enquanto o procedimento não foi gravado. */
    public Instant criadoEm() {
        return criadoEm;
    }

    /** Dados informativos de catálogo; nunca nulo (campos nulos em procedimentos anteriores). */
    public DadosCatalogo catalogo() {
        return catalogo;
    }
}
