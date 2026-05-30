package br.gov.seed.simulados;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret=test-secret-key-para-testes-unitarios-12345",
        // H2 em memória substitui o PostgreSQL
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        // Sem Redis real — desabilita Spring Session baseada em Redis
        "spring.session.store-type=none",
        "spring.data.redis.host=localhost",
        // Kafka sem broker — conexão é lazy, não falha no context load
        "spring.kafka.bootstrap-servers=localhost:9092",
        // Desabilita geração de docs OpenAPI no contexto de teste
        "springdoc.api-docs.enabled=false",
        "springdoc.swagger-ui.enabled=false"
})
class MsSimuladosApplicationTests {

    @Test
    void contextLoads() {
    }
}
