package br.org.sobei.denuncias;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@SpringBootApplication
public class DenunciasApplication {

    public static void main(String[] args) {
        loadEnv();
        SpringApplication.run(DenunciasApplication.class, args);
    }

    private static void resetDatabaseDirectly() {
        String host = System.getProperty("DB_HOST");
        String port = System.getProperty("DB_PORT");
        String name = System.getProperty("DB_NAME");
        String user = System.getProperty("DB_USER");
        String pass = System.getProperty("DB_PASSWORD");
        
        String url = "jdbc:postgresql://" + host + ":" + port + "/" + name;
        
        System.out.println(">>> Connecting to DB directly to drop tables: " + url);
        String sql = "DROP TABLE IF EXISTS flyway_schema_history CASCADE;\n" +
                     "DROP TABLE IF EXISTS historico_estados CASCADE;\n" +
                     "DROP TABLE IF EXISTS conclusoes_denuncia CASCADE;\n" +
                     "DROP TABLE IF EXISTS medidas_adotadas CASCADE;\n" +
                     "DROP TABLE IF EXISTS denunciantes_identificados CASCADE;\n" +
                     "DROP TABLE IF EXISTS denuncias CASCADE;\n" +
                     "DROP TABLE IF EXISTS administradores CASCADE;\n" +
                     "DROP TYPE IF EXISTS tipo_denuncia CASCADE;\n" +
                     "DROP TYPE IF EXISTS estado_denuncia CASCADE;\n" +
                     "DROP TYPE IF EXISTS tipo_conclusao CASCADE;";
                     
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(url, user, pass);
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println(">>> Direct database clean completed successfully.");
        } catch (Exception e) {
            System.err.println(">>> Direct clean failed: " + e.getMessage());
        }
    }

    private static void loadEnv() {
        Path envPath = Paths.get(".env");
        if (Files.exists(envPath)) {
            try {
                List<String> lines = Files.readAllLines(envPath);
                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    int eqIdx = line.indexOf('=');
                    if (eqIdx > 0) {
                        String key = line.substring(0, eqIdx).trim();
                        String value = line.substring(eqIdx + 1).trim();
                        
                        // Remover aspas duplas ou simples se existirem
                        if (value.startsWith("\"") && value.endsWith("\"")) {
                            value = value.substring(1, value.length() - 1);
                        } else if (value.startsWith("'") && value.endsWith("'")) {
                            value = value.substring(1, value.length() - 1);
                        }
                        
                        // Apenas define a propriedade se ela não foi definida no ambiente real
                        if (System.getProperty(key) == null && System.getenv(key) == null) {
                            System.setProperty(key, value);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Erro ao carregar o arquivo .env: " + e.getMessage());
            }
        }
    }

    public static void main() {
        System.out.println("Rodando");
    }
}
