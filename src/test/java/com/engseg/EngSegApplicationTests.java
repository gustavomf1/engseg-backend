package com.engseg;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Disabled("Requer PostgreSQL em execução")
@SpringBootTest
@ActiveProfiles("test")
class EngSegApplicationTests {

    @Test
    void contextLoads() {
    }
}
