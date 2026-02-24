package com.kazemieh.shop.identity.application

import com.kazemieh.shop.identity.application.exception.InvalidCredentialsException
import com.kazemieh.shop.identity.persistence.RefreshTokenRepository
import com.kazemieh.shop.identity.persistence.entity.RefreshTokenEntity
import com.kazemieh.shop.identity.persistence.entity.UserEntity
import com.kazemieh.shop.shared.security.jwt.JwtProperties
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.OffsetDateTime
import java.util.*

@Service
class RefreshTokenService(
    private val repo: RefreshTokenRepository,
    private val props: JwtProperties,
) {
    private val random = SecureRandom()

    private fun newRawToken(): String {
        val bytes = ByteArray(64)
        random.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun sha256Hex(raw: String): String {
        val d = MessageDigest.getInstance("SHA-256")
        val out = d.digest(raw.toByteArray(StandardCharsets.UTF_8))
        return out.joinToString("") { "%02x".format(it) }
    }

    @Transactional
    fun issueFor(user: UserEntity): String {
        val raw = newRawToken()
        val now = OffsetDateTime.now()
        val exp = now.plusDays(props.refreshTtlDays)

        repo.save(
            RefreshTokenEntity(
                user = user,
                tokenHash = sha256Hex(raw),
                expiresAt = exp,
                revokedAt = null
            )
        )
        return raw
    }

    @Transactional
    fun rotate(raw: String): UserEntity {
        val now = OffsetDateTime.now()
        val token = repo.findByTokenHash(sha256Hex(raw))
            ?: throw InvalidCredentialsException("Invalid refresh token")

        if (token.revokedAt != null || token.expiresAt.isBefore(now)) {
            throw InvalidCredentialsException("Invalid refresh token")
        }

        // revoke old
        token.revokedAt = now
        repo.save(token)

        // issue new (rotation)
        val user = token.user ?: throw InvalidCredentialsException("Invalid refresh token")

        return user
    }

    @Transactional
    fun revoke(raw: String) {
        val now = OffsetDateTime.now()
        val token = repo.findByTokenHash(sha256Hex(raw)) ?: return
        if (token.revokedAt == null) {
            token.revokedAt = now
            repo.save(token)
        }
    }

    @Transactional
    fun revokeAllForUser(userId: Long) {
        repo.revokeAllActiveForUser(userId, OffsetDateTime.now())
    }
}