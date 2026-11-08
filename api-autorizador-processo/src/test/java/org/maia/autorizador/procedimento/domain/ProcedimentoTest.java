package org.maia.autorizador.procedimento.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.maia.autorizador.procedimento.domain.model.Procedimento;
import org.maia.autorizador.procedimento.domain.model.Sexo;

class ProcedimentoTest {

    @Test
    void criaComSexosEmOrdemMFEIdadesAscendentes() {
        Procedimento p = Procedimento.create(10101, List.of(Sexo.F, Sexo.M), List.of(30, 18, 0));

        assertThat(p.id()).isNull();
        assertThat(p.numProcedimento()).isEqualTo(10101);
        assertThat(p.sexos()).containsExactly(Sexo.M, Sexo.F);
        assertThat(p.idades()).containsExactly(0, 18, 30);
    }

    @Test
    void aceitaListasVazias() {
        Procedimento p = Procedimento.create(1, List.of(), List.of());

        assertThat(p.sexos()).isEmpty();
        assertThat(p.idades()).isEmpty();
        assertThat(p.avaliar(10, Sexo.M)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void rejeitaNumeroMenorQueUm(int numero) {
        assertThatThrownBy(() -> Procedimento.create(numero, List.of(Sexo.M), List.of(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 151})
    void rejeitaIdadeForaDaFaixa(int idade) {
        assertThatThrownBy(() -> Procedimento.create(1, List.of(Sexo.M), List.of(idade)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aceitaOsLimitesDaFaixaDeIdade() {
        assertThat(Procedimento.create(1, List.of(), List.of(0, 150)).idades()).containsExactly(0, 150);
    }

    @Test
    void rejeitaDuplicatas() {
        assertThatThrownBy(() -> Procedimento.create(1, List.of(Sexo.M, Sexo.M), List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Procedimento.create(1, List.of(), List.of(5, 5)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejeitaColecoesNulas() {
        assertThatThrownBy(() -> Procedimento.create(1, null, List.of())).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Procedimento.create(1, List.of(), null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void avaliaExigindoSexoEIdade() {
        Procedimento p = Procedimento.create(1, List.of(Sexo.M), List.of(18, 30));

        assertThat(p.avaliar(18, Sexo.M)).isTrue();
        assertThat(p.avaliar(18, Sexo.F)).isFalse();
        assertThat(p.avaliar(19, Sexo.M)).isFalse();
        assertThat(p.avaliar(19, Sexo.F)).isFalse();
    }

    @Test
    void avaliaSexoSemDiferencaDeCaixa() {
        Procedimento p = Procedimento.create(1, List.of(Sexo.F), List.of(40));

        assertThat(p.avaliar(40, "f")).isTrue();
        assertThat(p.avaliar(40, "F")).isTrue();
        assertThat(p.avaliar(40, "m")).isFalse();
    }

    @Test
    void avaliarComSexoInvalidoFalha() {
        Procedimento p = Procedimento.create(1, List.of(Sexo.F), List.of(40));

        assertThatThrownBy(() -> p.avaliar(40, "X")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void restauraSemRevalidarIdentidade() {
        Instant agora = Instant.parse("2026-01-01T00:00:00Z");
        Procedimento p = Procedimento.restore(7L, 99, Set.of(Sexo.F, Sexo.M), Set.of(3, 1), agora);

        assertThat(p.id()).isEqualTo(7L);
        assertThat(p.sexos()).containsExactly(Sexo.M, Sexo.F);
        assertThat(p.idades()).containsExactly(1, 3);
        assertThat(p.criadoEm()).isEqualTo(agora);
    }

    @Test
    void colecoesExpostasSaoImutaveis() {
        Procedimento p = Procedimento.create(1, List.of(Sexo.M), List.of(1));

        assertThatThrownBy(() -> p.sexos().add(Sexo.F)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> p.idades().add(2)).isInstanceOf(UnsupportedOperationException.class);
    }
}
