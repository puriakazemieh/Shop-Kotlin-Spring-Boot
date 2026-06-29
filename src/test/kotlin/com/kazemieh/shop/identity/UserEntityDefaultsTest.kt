package com.kazemieh.shop.identity

import com.kazemieh.shop.identity.domain.UserRole
import com.kazemieh.shop.identity.persistence.entity.UserEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Guards the security fix: a newly created user must default to CUSTOMER,
 * never ADMIN. Pure unit test (no Spring context / DB).
 */
class UserEntityDefaultsTest {

    @Test
    fun `new user defaults to CUSTOMER role`() {
        val user = UserEntity(email = "customer@example.com", passwordHash = "hash")
        assertThat(user.role).isEqualTo(UserRole.CUSTOMER)
    }
}
