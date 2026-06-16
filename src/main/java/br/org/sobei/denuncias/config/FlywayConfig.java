package br.org.sobei.denuncias.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração da estratégia de migração do Flyway.
 *
 * Executa flyway.repair() antes de flyway.migrate() para alinhar automaticamente
 * os checksums do schema_history com os arquivos de migration locais.
 * Isso resolve erros de validação causados por ajustes seguros nos comentários
 * das migrations já aplicadas (ex: remoção de senhas em texto claro).
 */
@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy repairAndMigrateStrategy() {
        return (Flyway flyway) -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}
