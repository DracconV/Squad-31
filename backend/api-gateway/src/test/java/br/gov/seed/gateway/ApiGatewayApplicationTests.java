package br.gov.seed.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret=secret-para-testes-unitarios-nao-usado-em-producao"
})
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
    }
}