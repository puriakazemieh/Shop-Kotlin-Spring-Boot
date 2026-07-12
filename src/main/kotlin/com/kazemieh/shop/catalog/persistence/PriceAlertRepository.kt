package com.kazemieh.shop.catalog.persistence

import com.kazemieh.shop.catalog.persistence.entity.PriceAlertEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PriceAlertRepository : JpaRepository<PriceAlertEntity, Long> {
    fun findByVariantIdAndUserId(variantId: Long, userId: Long): PriceAlertEntity?
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): List<PriceAlertEntity>
    fun findAllByVariantIdAndNotifiedFalseAndTargetPriceGreaterThanEqual(
        variantId: Long,
        targetPrice: java.math.BigDecimal
    ): List<PriceAlertEntity>
}
