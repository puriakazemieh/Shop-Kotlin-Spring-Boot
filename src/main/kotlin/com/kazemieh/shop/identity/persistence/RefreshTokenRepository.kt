package com.kazemieh.shop.identity.persistence

import com.kazemieh.shop.identity.persistence.entity.RefreshTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime

interface RefreshTokenRepository : JpaRepository<RefreshTokenEntity, Long> {
    fun findByTokenHash(tokenHash: String): RefreshTokenEntity?

    @Modifying
    @Query(
        """
        update RefreshTokenEntity t
           set t.revokedAt = :now
         where t.user.id = :userId
           and t.revokedAt is null
        """
    )
    fun revokeAllActiveForUser(@Param("userId") userId: Long, @Param("now") now: OffsetDateTime): Int
}