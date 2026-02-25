package com.kazemieh.shop.cart.persistence

import com.kazemieh.shop.cart.persistence.entity.CartItemEntity
import org.springframework.data.jpa.repository.JpaRepository

interface CartItemRepository : JpaRepository<CartItemEntity, Long> {
    fun findByCartIdAndVariantId(cartId: Long, variantId: Long): CartItemEntity?
    fun findByIdAndCartId(id: Long, cartId: Long): CartItemEntity?
    fun deleteAllByCartId(cartId: Long): Long
}