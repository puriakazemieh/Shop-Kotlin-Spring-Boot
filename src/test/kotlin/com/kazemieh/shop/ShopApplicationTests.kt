package com.kazemieh.shop

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class ShopApplicationTests {

    companion object {
        @Container
        @ServiceConnection
        val postgresContainer = PostgreSQLContainer("postgres:15-alpine")
    }

    @Test
    fun contextLoads() {
    }

}
