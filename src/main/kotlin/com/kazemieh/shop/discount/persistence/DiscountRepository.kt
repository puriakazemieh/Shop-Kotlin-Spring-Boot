package com.kazemieh.shop.discount.persistence

import com.kazemieh.shop.discount.persistence.entity.DiscountEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface DiscountRepository : JpaRepository<DiscountEntity, Long> {
    fun findByCode(code: String): Optional<DiscountEntity>
}
