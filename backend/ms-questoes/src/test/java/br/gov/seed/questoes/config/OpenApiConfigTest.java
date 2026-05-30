package br.gov.seed.questoes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OpenApiConfig — testes unitários")
class OpenApiConfigTest {

    private final OpenApiConfig config = new OpenApiConfig();

    @Test
    @DisplayName("openAPI() retorna bean não nulo com título correto")
    void openAPI_retornaTituloCorreto() {
        OpenAPI api = config.openAPI();

        assertThat(api).isNotNull();
        assertThat(api.getInfo()).isNotNull();
        assertThat(api.getInfo().getTitle()).contains("ms-questoes");
        assertThat(api.getInfo().getVersion()).isEqualTo("1.0");
    }

    @Test
    @DisplayName("openAPI() registra esquema de segurança BearerAuth JWT")
    void openAPI_registraSecuritySchemeBearer() {
        OpenAPI api = config.openAPI();

        assertThat(api.getComponents()).isNotNull();
        assertThat(api.getComponents().getSecuritySchemes())
                .containsKey("BearerAuth");

        SecurityScheme scheme = api.getComponents().getSecuritySchemes().get("BearerAuth");
        assertThat(scheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(scheme.getScheme()).isEqualTo("bearer");
        assertThat(scheme.getBearerFormat()).isEqualTo("JWT");
    }
}
