package com.kazemieh.shop.wallet.api.dto

import com.kazemieh.shop.wallet.persistence.entity.TransactionType
import com.kazemieh.shop.wallet.persistence.entity.WithdrawalStatus
import java.math.BigDecimal
import java.time.OffsetDateTime

data class WalletResponse(
    val balance: BigDecimal,
    val userId: Long
)

data class WalletTransactionResponse(
    val id: Long,
    val amount: BigDecimal,
    val type: TransactionType,
    val description: String?,
    val referenceId: String?,
    val createdAt: OffsetDateTime?
)

data class TopUpRequest(
    val amount: BigDecimal
)

data class WithdrawalRequest(
    val amount: BigDecimal,
    val iban: String
)

data class WithdrawalRequestResponse(
    val id: Long,
    val amount: BigDecimal,
    val iban: String,
    val status: WithdrawalStatus,
    val adminNote: String?,
    val createdAt: OffsetDateTime?
)

data class AdminAdjustBalanceRequest(
    val userId: Long,
    val amount: BigDecimal,
    val description: String? = null
)

data class AdminProcessWithdrawalRequest(
    val status: WithdrawalStatus,
    val adminNote: String? = null
)
