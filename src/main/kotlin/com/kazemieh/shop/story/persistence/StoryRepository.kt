package com.kazemieh.shop.story.persistence

import com.kazemieh.shop.story.persistence.entity.StoryEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
interface StoryRepository : JpaRepository<StoryEntity, Long> {
    fun findAllByIsActiveTrueAndExpiresAtAfterOrderByCreatedAtDesc(now: OffsetDateTime): List<StoryEntity>
}
