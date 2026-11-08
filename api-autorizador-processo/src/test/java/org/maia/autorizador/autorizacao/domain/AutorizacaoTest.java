package org.maia.autorizador.autorizacao.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.maia.autorizador.autorizacao.domain.exception.AutorizacaoNegadaException;
import org.maia.autorizador.autorizacao.domain.model.Autorizacao;
import org.maia.autorizador.procedimento.domain.model.Procedimento;
import org.maia.autorizador.procedimento.domain.model.Sexo;
import org.maia.autorizador.shared.error.BusinessRuleException;

class AutorizacaoTest {

    private static Procedimento gravado() {
        return Procedimento.restore(7L, 4567, List.of(Sexo.F), List.of(20), Instant.parse("2026-01-01T00:00:00Z"));
    }

    @Test
    void concederGeraAutorizacaoSempreComPermitidoVerdadeiro() {
        Autorizacao a = Autorizacao.conceder(gravado());

        assertThat(a.id()).isNull();
        assertThat(a.procedimentoId()).isEqualTo(7L);
        assertThat(a.permitido()).isTrue();
        assertThat(a.criadaEm()).isNull();
    }

    @Test
    void concederExigeProcedimentoJaGravado() {
        Procedimento novo = Procedimento.create(1, List.of(Sexo.M), List.of(10));

        assertThatThrownBy(() -> Autorizacao.conceder(novo)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Autorizacao.conceder(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void restoreReidrataSemReaplicarRegras() {
        Instant criada = Instant.parse("2026-02-02T10:00:00Z");

        Autorizacao a = Autorizacao.restore(3L, 7L, true, criada);

        assertThat(a.id()).isEqualTo(3L);
        assertThat(a.procedimentoId()).isEqualTo(7L);
        assertThat(a.permitido()).isTrue();
        assertThat(a.criadaEm()).isEqualTo(criada);
        assertThat(Autorizacao.restore(4L, 7L, false, null).criadaEm()).isNull();
    }

    @Test
    void negativaTemCodigoECategoriaDe403() {
        var ex = AutorizacaoNegadaException.para(1234);

        assertThat(ex).isInstanceOf(BusinessRuleException.class);
        assertThat(ex.getCode()).isEqualTo("AUTORIZACAO_NEGADA");
        assertThat(ex.getCategoria()).isEqualTo(BusinessRuleException.Categoria.OPERACAO_NAO_PERMITIDA);
        assertThat(ex.getMessage()).contains("1234").doesNotContain("idade", "sexo");
    }
}
