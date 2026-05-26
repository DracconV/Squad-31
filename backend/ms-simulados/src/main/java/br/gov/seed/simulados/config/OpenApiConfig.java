package br.gov.seed.simulados.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SEED Educa — ms-simulados")
                        .version("1.0")
                        .description("""
                                API de simulados do SEED Educa.
                                Gerencie sessões de simulado, registre respostas e consulte resultados.
                                Autentique-se com o token JWT obtido em /auth/login.
                                """))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Informe: Bearer {seu-token}")));
    }
}
