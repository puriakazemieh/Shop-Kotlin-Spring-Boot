package com.kazemieh.shop.catalog.persistence

import com.kazemieh.shop.catalog.persistence.entity.BannerEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BannerRepository : JpaRepository<BannerEntity, Long> {
    fun findAllByIsActiveTrueOrderBySortOrderAsc(): List<BannerEntity>

    fun findAllByOrderBySortOrderAsc(): List<BannerEntity>
}
