package com.kazemieh.shop.order.persistence

import com.kazemieh.shop.order.persistence.entity.OrderItemEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderItemRepository : JpaRepository<OrderItemEntity, Long> {
    fun existsByVariantId(variantId: Long): Boolean
}
