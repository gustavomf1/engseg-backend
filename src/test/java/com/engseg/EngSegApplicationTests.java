package com.engseg;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// Requer banco de dados em execução — use os testes unitários (@ExtendWith(MockitoExtension))
// e de slice (@WebMvcTest) para rodar sem infraestrutura.
@Disabled("Requer PostgreSQL em execução")
@SpringBootTest
@ActiveProfiles("test")
class EngSegApplicationTests {

    @Test
    void contextLoads() {
    }
}
