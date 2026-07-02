package com.kazemieh.shop.catalog.persistence

import com.kazemieh.shop.catalog.persistence.entity.ProductReviewHelpfulEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ProductReviewHelpfulRepository : JpaRepository<ProductReviewHelpfulEntity, Long> {

    fun findByReviewIdAndUserId(reviewId: Long, userId: Long): ProductReviewHelpfulEntity?

    /**
     * شناسه‌ی همه‌ی نظرهای یک محصول که کاربرِ داده‌شده آن‌ها را «مفید» علامت زده
     * (شاملِ نظرهای سطح‌اول و پاسخ‌ها) — برای پرکردنِ فیلدِ helpfulByMe.
     */
    @Query(
        """
        SELECT h.review.id FROM ProductReviewHelpfulEntity h
        WHERE h.userId = :userId AND h.review.product.id = :productId
        """
    )
    fun findReviewIdsByUserAndProduct(userId: Long, productId: Long): List<Long>
}
