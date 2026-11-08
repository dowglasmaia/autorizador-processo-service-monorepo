package org.maia.autorizador.support;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Esvazia as tabelas de negócio e de auditoria entre testes, reiniciando as identidades.
 * Preserva {@code flyway_schema_history}.
 */
public final class LimpezaBanco {

    private static final String TABELAS_PUBLICAS = """
            select quote_ident(tablename) from pg_tables
            where schemaname = current_schema() and tablename <> 'flyway_schema_history'
            """;

    private final JdbcTemplate jdbc;

    public LimpezaBanco(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void limpar() {
        List<String> tabelas = jdbc.queryForList(TABELAS_PUBLICAS, String.class);
        if (!tabelas.isEmpty()) {
            jdbc.execute("truncate table " + String.join(", ", tabelas) + " restart identity cascade");
        }
    }
}
