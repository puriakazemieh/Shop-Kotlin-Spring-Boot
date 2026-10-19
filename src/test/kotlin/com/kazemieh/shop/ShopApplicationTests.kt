package com.kazemieh.shop

import com.kazemieh.shop.identity.api.dto.LogoutRequest
import com.kazemieh.shop.identity.api.dto.RefreshRequest
import com.kazemieh.shop.identity.application.AuthService
import com.kazemieh.shop.identity.application.RefreshTokenService
import com.kazemieh.shop.identity.application.exception.InvalidCredentialsException
import com.kazemieh.shop.identity.persistence.UserRepository
import com.kazemieh.shop.identity.persistence.entity.UserEntity
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
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

    @Autowired lateinit var users: UserRepository
    @Autowired lateinit var refreshTokens: RefreshTokenService
    @Autowired lateinit var auth: AuthService

    companion object {
        @Container
        @ServiceConnection
        val postgresContainer = PostgreSQLContainer("postgres:15-alpine")
    }

    @Test
    fun contextLoads() {
    }

    @Test
    fun `stale logout does not revoke rotated session in transactional database`() {
        val user = users.save(UserEntity(email = "session-characterization@example.invalid", passwordHash = "synthetic"))
        val old = refreshTokens.issueFor(user)
        val replacement = auth.refresh(RefreshRequest(old)).refreshToken
        auth.logout(LogoutRequest(old))

        assertThatThrownBy { auth.refresh(RefreshRequest(old)) }.isInstanceOf(InvalidCredentialsException::class.java)
        // Characterization of the current defect, not the intended logout contract.
        assertThat(auth.refresh(RefreshRequest(replacement)).refreshToken).isNotBlank()
    }

}
