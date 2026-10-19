package com.kazemieh.shop.identity.application

import com.kazemieh.shop.identity.application.exception.InvalidCredentialsException
import com.kazemieh.shop.identity.persistence.RefreshTokenRepository
import com.kazemieh.shop.identity.persistence.entity.RefreshTokenEntity
import com.kazemieh.shop.identity.persistence.entity.UserEntity
import com.kazemieh.shop.shared.security.jwt.JwtProperties
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

/** Characterizes existing behavior; the replacement surviving logout is a known security defect. */
class RefreshLogoutOrderingCharacterizationTest {
    @Test
    fun `logout using a rotated token leaves its replacement usable`() {
        val stored = mutableMapOf<String, RefreshTokenEntity>()
        val repository = mockk<RefreshTokenRepository>()
        every { repository.save(any<RefreshTokenEntity>()) } answers {
            firstArg<RefreshTokenEntity>().also { stored[it.tokenHash] = it }
        }
        every { repository.findByTokenHash(any()) } answers { stored[firstArg<String>()] }
        val service = RefreshTokenService(repository, JwtProperties("test-only", "synthetic", 5, 1))
        val syntheticUser = UserEntity(id = 1)

        val old = service.issueFor(syntheticUser)
        assertThat(service.rotate(old)).isSameAs(syntheticUser)
        val replacement = service.issueFor(syntheticUser)
        service.revoke(old)

        assertThatThrownBy { service.rotate(old) }.isInstanceOf(InvalidCredentialsException::class.java)
        // This documents the current bug, not the desired security behavior.
        assertThat(service.rotate(replacement)).isSameAs(syntheticUser)
    }
}
