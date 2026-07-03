package com.kazemieh.shop.psychtest.persistence

import com.kazemieh.shop.psychtest.persistence.entity.PsychTestEntity
import com.kazemieh.shop.psychtest.persistence.entity.UserPsychTestEntity
import com.kazemieh.shop.psychtest.persistence.entity.UserTestStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PsychTestRepository : JpaRepository<PsychTestEntity, Long> {
    fun findAllByIsPublishedTrueOrderByCreatedAtDesc(): List<PsychTestEntity>
    fun findBySlug(slug: String): PsychTestEntity?
    fun existsBySlug(slug: String): Boolean
    /** تست‌هایی که به این محصولات لینک شده‌اند (برای اعطای دسترسی پس از خرید). */
    fun findAllByProductIdIn(productIds: Collection<Long>): List<PsychTestEntity>
}

@Repository
interface UserPsychTestRepository : JpaRepository<UserPsychTestEntity, Long> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): List<UserPsychTestEntity>
    fun findByIdAndUserId(id: Long, userId: Long): UserPsychTestEntity?
    fun existsByUserIdAndTestIdAndStatus(userId: Long, testId: Long, status: UserTestStatus): Boolean
    fun findAllByStatus(status: UserTestStatus): List<UserPsychTestEntity>
}
