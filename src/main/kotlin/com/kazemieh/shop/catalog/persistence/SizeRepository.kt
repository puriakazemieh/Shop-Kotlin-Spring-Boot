package com.kazemieh.shop.catalog.persistence

import com.kazemieh.shop.catalog.persistence.entity.SizeEntity
import org.springframework.data.jpa.repository.JpaRepository

interface SizeRepository : JpaRepository<SizeEntity, Long> {
    fun findAllByOrderBySortOrderAscNameAsc(): List<SizeEntity>
}