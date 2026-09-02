package br.org.sobei.denuncias.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            // Executa repair para alinhar checksums de migrações e em seguida executa migrate
            flyway.repair();
            flyway.migrate();
        };
    }
}
