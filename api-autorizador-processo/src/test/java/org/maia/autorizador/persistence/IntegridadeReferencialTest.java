package org.maia.autorizador.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.maia.autorizador.support.IntegrationTestBase;
import org.springframework.dao.DataIntegrityViolationException;

class IntegridadeReferencialTest extends IntegrationTestBase {

    private long criarProcedimento(int numero) {
        return jdbc.queryForObject(
                "insert into procedimento (numero_procedimento) values (?) returning id", Long.class, numero);
    }

    @Test
    void fkImpedeApagarProcedimentoComAutorizacao() {
        long id = criarProcedimento(1234);
        jdbc.update("insert into autorizacao (procedimento_id, permitido) values (?, true)", id);

        assertThatThrownBy(() -> jdbc.update("delete from procedimento where id = ?", id))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(jdbc.queryForObject("select count(*) from procedimento", Integer.class)).isEqualTo(1);
    }

    @Test
    void autorizacaoExigeProcedimentoExistente() {
        assertThatThrownBy(() -> jdbc.update(
                "insert into autorizacao (procedimento_id, permitido) values (999999, true)"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void apagarProcedimentoRemoveSexosEFaixasEmCascata() {
        long id = criarProcedimento(1234);
        jdbc.update("insert into procedimento_sexo (procedimento_id, sexo) values (?, 'M'), (?, 'F')", id, id);
        jdbc.update("insert into procedimento_faixa_etaria (procedimento_id, faixa) values (?, 'CRIANCA'), (?, 'IDOSO')", id, id);

        jdbc.update("delete from procedimento where id = ?", id);

        assertThat(jdbc.queryForObject("select count(*) from procedimento_sexo", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from procedimento_faixa_etaria", Integer.class)).isZero();
    }
}
