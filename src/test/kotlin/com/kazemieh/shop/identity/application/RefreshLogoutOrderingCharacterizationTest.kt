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

/** Regression test: all rotated credentials in a session family are revoked together. */
class RefreshLogoutOrderingCharacterizationTest {
    @Test
    fun `logout using a rotated token revokes its replacement family`() {
        val stored = mutableMapOf<String, RefreshTokenEntity>()
        val repository = mockk<RefreshTokenRepository>()
        every { repository.save(any<RefreshTokenEntity>()) } answers {
            firstArg<RefreshTokenEntity>().also { stored[it.tokenHash] = it }
        }
        every { repository.findByTokenHash(any()) } answers { stored[firstArg<String>()] }
        every { repository.revokeAllActiveForFamily(any(), any()) } answers {
            val family = firstArg<String>()
            stored.values.filter { it.familyId == family && it.revokedAt == null }.forEach { it.revokedAt = secondArg() }
            1
        }
        val service = RefreshTokenService(repository, JwtProperties("test-only", "synthetic", 5, 1))
        val syntheticUser = UserEntity(id = 1)

        val old = service.issueFor(syntheticUser)
        assertThat(service.rotate(old)).isSameAs(syntheticUser)
        val replacement = service.issueFor(syntheticUser, stored.values.single().familyId)
        service.revoke(old)

        assertThatThrownBy { service.rotate(old) }.isInstanceOf(InvalidCredentialsException::class.java)
        assertThatThrownBy { service.rotate(replacement) }.isInstanceOf(InvalidCredentialsException::class.java)
    }
}
