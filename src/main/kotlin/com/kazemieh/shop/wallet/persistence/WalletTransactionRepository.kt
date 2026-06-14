package com.kazemieh.shop.wallet.persistence

import com.kazemieh.shop.wallet.persistence.entity.WalletTransactionEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WalletTransactionRepository : JpaRepository<WalletTransactionEntity, Long> {
    fun findAllByWalletIdOrderByCreatedAtDesc(walletId: Long, pageable: Pageable): Page<WalletTransactionEntity>
}
