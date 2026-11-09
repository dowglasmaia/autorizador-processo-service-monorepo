package org.maia.autorizador.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.maia.autorizador.support.IntegrationTestBase;
import org.springframework.dao.DataIntegrityViolationException;

class RestricoesBancoTest extends IntegrationTestBase {

    private long criarProcedimento(int numero) {
        return jdbc.queryForObject(
                "insert into procedimento (numero_procedimento) values (?) returning id", Long.class, numero);
    }

    @Test
    void numeroProcedimentoEUnico() {
        criarProcedimento(1234);
        assertThatThrownBy(() -> criarProcedimento(1234)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void numeroProcedimentoDeveSerPositivo() {
        assertThatThrownBy(() -> criarProcedimento(0)).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> criarProcedimento(-5)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void chavePrimariaCompostaRejeitaSexoDuplicado() {
        long id = criarProcedimento(1);
        jdbc.update("insert into procedimento_sexo (procedimento_id, sexo) values (?, 'M')", id);
        assertThatThrownBy(() -> jdbc.update(
                "insert into procedimento_sexo (procedimento_id, sexo) values (?, 'M')", id))
                .isInstanceOf(DataIntegrityViolationException.class);
        jdbc.update("insert into procedimento_sexo (procedimento_id, sexo) values (?, 'F')", id);
    }

    @Test
    void chavePrimariaCompostaRejeitaFaixaDuplicada() {
        long id = criarProcedimento(1);
        jdbc.update("insert into procedimento_faixa_etaria (procedimento_id, faixa) values (?, 'CRIANCA')", id);
        assertThatThrownBy(() -> jdbc.update(
                "insert into procedimento_faixa_etaria (procedimento_id, faixa) values (?, 'CRIANCA')", id))
                .isInstanceOf(DataIntegrityViolationException.class);
        jdbc.update("insert into procedimento_faixa_etaria (procedimento_id, faixa) values (?, 'IDOSO')", id);
    }

    @Test
    void sexoDeveSerMOuF() {
        long id = criarProcedimento(1);
        assertThatThrownBy(() -> jdbc.update(
                "insert into procedimento_sexo (procedimento_id, sexo) values (?, 'X')", id))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void faixaDeveSerUmaDasQuatroFaixasEtarias() {
        long id = criarProcedimento(1);
        for (String faixa : new String[] {"CRIANCA", "ADOLESCENTE", "ADULTO", "IDOSO"}) {
            jdbc.update("insert into procedimento_faixa_etaria (procedimento_id, faixa) values (?, ?)", id, faixa);
        }
        assertThatThrownBy(() -> jdbc.update(
                "insert into procedimento_faixa_etaria (procedimento_id, faixa) values (?, 'BEBE')", id))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(jdbc.queryForObject("select count(*) from procedimento_faixa_etaria", Integer.class)).isEqualTo(4);
    }
}
