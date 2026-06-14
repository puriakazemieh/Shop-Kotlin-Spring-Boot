package com.kazemieh.shop.wallet.persistence

import com.kazemieh.shop.wallet.persistence.entity.WalletEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WalletRepository : JpaRepository<WalletEntity, Long> {
    fun findByUserId(userId: Long): WalletEntity?
}
