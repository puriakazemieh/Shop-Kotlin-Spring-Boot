package com.kazemieh.shop.cart.persistence

import com.kazemieh.shop.cart.persistence.entity.CartEntity
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface CartRepository : JpaRepository<CartEntity, Long> {

    fun findByUserId(userId: Long): CartEntity?

    @EntityGraph(attributePaths = ["items"])
    fun findWithItemsByUserId(userId: Long): CartEntity?
}