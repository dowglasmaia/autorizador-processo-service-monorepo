package org.maia.autorizador.procedimento.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.maia.autorizador.procedimento.domain.model.Sexo;

class SexoTest {

    @ParameterizedTest
    @ValueSource(strings = {"M", "m", " m "})
    void parseAceitaMasculinoEmQualquerCaixa(String valor) {
        assertThat(Sexo.parse(valor)).isEqualTo(Sexo.M);
    }

    @ParameterizedTest
    @ValueSource(strings = {"F", "f"})
    void parseAceitaFemininoEmQualquerCaixa(String valor) {
        assertThat(Sexo.parse(valor)).isEqualTo(Sexo.F);
    }

    @ParameterizedTest
    @ValueSource(strings = {"X", "", "MF", " "})
    void parseRejeitaValoresInvalidos(String valor) {
        assertThatThrownBy(() -> Sexo.parse(valor)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void parseRejeitaNulo() {
        assertThatThrownBy(() -> Sexo.parse(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
