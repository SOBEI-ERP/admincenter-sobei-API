package br.org.sobei.denuncias.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SOBEI Denúncias API")
                        .version("1.0.0")
                        .description("API REST para o sistema de denúncias da Sociedade Beneficente Equilíbrio de Interlagos (SOBEI). Permite a abertura e acompanhamento de denúncias, de forma anônima ou identificada.")
                        .contact(new Contact()
                                .name("Suporte SOBEI")
                                .url("https://www.sobei.org.br"))
                        .license(new License().name("Proprietário").url("https://www.sobei.org.br")))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
