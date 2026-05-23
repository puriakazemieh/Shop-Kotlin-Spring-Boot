package com.kazemieh.shop.catalog.persistence

import com.kazemieh.shop.catalog.persistence.entity.OptionValueEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface OptionValueRepository : JpaRepository<OptionValueEntity, Long> {
    fun findByOptionTypeIdAndValue(optionTypeId: Long, value: String): Optional<OptionValueEntity>
}
