package com.kazemieh.shop.catalog.persistence

import com.kazemieh.shop.catalog.persistence.entity.InventoryEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface InventoryRepository : JpaRepository<InventoryEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InventoryEntity i where i.variantId in :ids")
    fun findAllForUpdate(@Param("ids") ids: List<Long>): List<InventoryEntity>
}