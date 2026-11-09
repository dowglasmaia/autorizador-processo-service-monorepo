package org.maia.autorizador.shared.persistence;

import org.flywaydb.core.Flyway;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Conveniência do perfil {@code local}: a seed de desenvolvimento é uma migração versionada (V1000) que pode ser
 * revisada junto com o esquema. Num banco local criado antes da revisão, o Flyway acusa checksum diferente e
 * migrações novas "fora de ordem" (ex.: V5 depois da V1000). Aqui o checksum é reparado e as pendentes são aplicadas.
 * Nunca ativo em outros perfis: lá uma divergência de checksum deve falhar a subida.
 */
@Configuration
@Profile("local")
public class FlywayLocalConfig {

    @Bean
    FlywayMigrationStrategy migracaoLocalComReparo() {
        return FlywayLocalConfig::reparar;
    }

    public static void reparar(Flyway flyway) {
        flyway.repair();
        flyway.migrate();
    }
}
