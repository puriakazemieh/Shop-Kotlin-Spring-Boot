package com.kazemieh.shop.identity.persistence

import com.kazemieh.shop.identity.domain.UserRole
import com.kazemieh.shop.identity.persistence.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<UserEntity, Long> {
    fun countByRole(role: UserRole): Long
    fun findByEmail(email: String): UserEntity?
    fun findByPhone(phone: String): UserEntity?
    fun findByEmailOrPhone(email: String, phone: String): UserEntity?
    fun existsByEmail(email: String): Boolean
    fun existsByPhone(phone: String): Boolean
    fun findByResetPasswordToken(token: String): Optional<UserEntity>
    fun findByReferralCode(referralCode: String): UserEntity?
    fun countByReferredByUserId(referrerId: Long): Long
}
