package org.maia.autorizador.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.maia.autorizador.support.IntegrationTestBase;

class MigracaoTest extends IntegrationTestBase {

    @Test
    void migracaoLimpaAplicaV1AV5ComSucesso() {
        List<Map<String, Object>> historico = jdbc.queryForList(
                "select version, success from flyway_schema_history order by installed_rank");

        assertThat(historico).hasSize(5);
        assertThat(historico).extracting(l -> l.get("version")).containsExactly("1", "2", "3", "4", "5");
        assertThat(historico).extracting(l -> l.get("success")).containsOnly(true);
    }
}
