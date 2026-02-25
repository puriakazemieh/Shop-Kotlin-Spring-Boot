package com.kazemieh.shop.catalog.persistence

import com.kazemieh.shop.catalog.persistence.entity.ColorEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ColorRepository : JpaRepository<ColorEntity, Long> {
    fun findAllByOrderByNameAsc(): List<ColorEntity>
}