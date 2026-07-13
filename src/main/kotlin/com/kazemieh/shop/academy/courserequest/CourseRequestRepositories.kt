package com.kazemieh.shop.academy.courserequest

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CourseRequestRepository : JpaRepository<CourseRequestEntity, Long> {
    /** برای نمایشِ عمومی/ادمین: پرلایک‌ترین‌ها بالاتر، سپس جدیدترین‌ها. */
    fun findAllByOrderByLikeCountDescCreatedAtDesc(): List<CourseRequestEntity>

    /** درخواست‌های ثبت‌شده توسطِ یک کاربر. */
    fun findAllByRequesterUserIdOrderByCreatedAtDesc(userId: Long): List<CourseRequestEntity>
}

@Repository
interface CourseRequestVoteRepository : JpaRepository<CourseRequestVoteEntity, Long> {
    fun findByRequestIdAndUserId(requestId: Long, userId: Long): CourseRequestVoteEntity?
    fun deleteByRequestIdAndUserId(requestId: Long, userId: Long)
    fun deleteAllByRequestId(requestId: Long)
    fun findAllByUserId(userId: Long): List<CourseRequestVoteEntity>
}
