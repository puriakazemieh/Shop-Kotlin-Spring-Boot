package com.kazemieh.shop.identity.persistence

import com.kazemieh.shop.identity.persistence.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByEmail(email: String): UserEntity?
    fun existsByEmail(email: String): Boolean
    fun findByResetPasswordToken(token: String): Optional<UserEntity>
}
