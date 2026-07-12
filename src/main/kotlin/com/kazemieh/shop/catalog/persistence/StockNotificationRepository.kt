package com.kazemieh.shop.catalog.persistence

import com.kazemieh.shop.catalog.persistence.entity.StockNotificationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StockNotificationRepository : JpaRepository<StockNotificationEntity, Long> {

    fun findByVariantIdAndUserId(variantId: Long, userId: Long): StockNotificationEntity?

    fun findAllByVariantIdAndNotifiedFalse(variantId: Long): List<StockNotificationEntity>

    fun existsByVariantIdAndUserIdAndNotifiedFalse(variantId: Long, userId: Long): Boolean
}
