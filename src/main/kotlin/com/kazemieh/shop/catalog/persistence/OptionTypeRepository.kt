package com.kazemieh.shop.catalog.persistence

import com.kazemieh.shop.catalog.persistence.entity.OptionTypeEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface OptionTypeRepository : JpaRepository<OptionTypeEntity, Long> {
    fun findByName(name: String): Optional<OptionTypeEntity>
}
