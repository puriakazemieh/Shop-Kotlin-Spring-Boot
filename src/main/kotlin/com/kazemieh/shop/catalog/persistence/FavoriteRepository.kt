package com.kazemieh.shop.catalog.persistence

import com.kazemieh.shop.catalog.persistence.entity.FavoriteEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FavoriteRepository : JpaRepository<FavoriteEntity, Long> {
    fun existsByUserIdAndProductId(userId: Long, productId: Long): Boolean
    fun deleteByUserIdAndProductId(userId: Long, productId: Long)
    fun findAllByUserId(userId: Long, pageable: Pageable): Page<FavoriteEntity>
    fun findByUserIdAndProductId(userId: Long, productId: Long): FavoriteEntity?
    fun findAllByUserId(userId: Long): List<FavoriteEntity>
    fun findAllByUserIdAndProductIdIn(userId: Long, productIds: List<Long>): List<FavoriteEntity>
    fun deleteAllByProductId(productId: Long)
}
