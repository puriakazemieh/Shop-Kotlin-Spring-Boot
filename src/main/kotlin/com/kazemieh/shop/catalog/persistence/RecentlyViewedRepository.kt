package com.kazemieh.shop.catalog.persistence

import com.kazemieh.shop.catalog.persistence.entity.RecentlyViewedEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RecentlyViewedRepository : JpaRepository<RecentlyViewedEntity, Long> {
    fun findByUserIdAndProductId(userId: Long, productId: Long): RecentlyViewedEntity?
    fun findAllByUserIdOrderByViewedAtDesc(userId: Long, pageable: Pageable): Page<RecentlyViewedEntity>
    fun deleteAllByProductId(productId: Long)
}
