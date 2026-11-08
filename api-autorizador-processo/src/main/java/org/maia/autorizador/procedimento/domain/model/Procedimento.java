package org.maia.autorizador.procedimento.domain.model;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Procedimento autorizável: o número é imutável e as listas de sexos e idades aceitos podem ser vazias (RN9).
 * Construído por {@link #create} (aplica as invariantes) ou {@link #restore} (reidrata o já gravado); sem setters.
 */
public final class Procedimento {

    public static final int IDADE_MINIMA = 0;
    public static final int IDADE_MAXIMA = 150;

    private final Long id;
    private final int numProcedimento;
    private final Set<Sexo> sexos;
    private final Set<Integer> idades;
    private final Instant criadoEm;

    private Procedimento(Long id, int numProcedimento, Set<Sexo> sexos, Set<Integer> idades, Instant criadoEm) {
        this.id = id;
        this.numProcedimento = numProcedimento;
        this.sexos = Collections.unmodifiableSet(sexos);
        this.idades = Collections.unmodifiableSet(idades);
        this.criadoEm = criadoEm;
    }

    /** Novo procedimento, ainda sem identificador nem data (atribuídos na persistência). */
    public static Procedimento create(int numProcedimento, Collection<Sexo> sexos, Collection<Integer> idades) {
        if (numProcedimento < 1) {
            throw new IllegalArgumentException("O número do procedimento deve ser maior ou igual a 1.");
        }
        Objects.requireNonNull(sexos, "sexos");
        Objects.requireNonNull(idades, "idades");
        Set<Sexo> sexosOrdenados = EnumSet.noneOf(Sexo.class);
        for (Sexo sexo : sexos) {
            if (!sexosOrdenados.add(Objects.requireNonNull(sexo, "sexo"))) {
                throw new IllegalArgumentException("Sexos duplicados.");
            }
        }
        Set<Integer> idadesOrdenadas = new TreeSet<>();
        for (Integer idade : idades) {
            Objects.requireNonNull(idade, "idade");
            if (idade < IDADE_MINIMA || idade > IDADE_MAXIMA) {
                throw new IllegalArgumentException(
                        "Idade fora da faixa " + IDADE_MINIMA + ".." + IDADE_MAXIMA + ": " + idade);
            }
            if (!idadesOrdenadas.add(idade)) {
                throw new IllegalArgumentException("Idades duplicadas.");
            }
        }
        return new Procedimento(null, numProcedimento, sexosOrdenados, idadesOrdenadas, null);
    }

    /** Reidrata um procedimento gravado (sem reaplicar a criação). */
    public static Procedimento restore(long id, int numProcedimento, Collection<Sexo> sexos,
            Collection<Integer> idades, Instant criadoEm) {
        Set<Sexo> sexosOrdenados = EnumSet.noneOf(Sexo.class);
        sexosOrdenados.addAll(sexos);
        return new Procedimento(id, numProcedimento, sexosOrdenados, new TreeSet<>(idades), criadoEm);
    }

    /** RN5–RN7: autoriza quando o sexo está entre os aceitos E a idade está entre as aceitas. */
    public boolean avaliar(int idade, Sexo sexo) {
        return sexos.contains(Objects.requireNonNull(sexo, "sexo")) && idades.contains(idade);
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

    /** Idades aceitas, em ordem crescente; somente leitura. */
    public Set<Integer> idades() {
        return idades;
    }

    /** Nulo enquanto o procedimento não foi gravado. */
    public Instant criadoEm() {
        return criadoEm;
    }
}
