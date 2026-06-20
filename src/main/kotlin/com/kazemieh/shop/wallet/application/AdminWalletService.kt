package com.kazemieh.shop.wallet.application

import com.kazemieh.shop.identity.persistence.UserRepository
import com.kazemieh.shop.wallet.api.dto.*
import com.kazemieh.shop.wallet.persistence.WithdrawalRequestRepository
import com.kazemieh.shop.wallet.persistence.entity.TransactionType
import com.kazemieh.shop.wallet.persistence.entity.WithdrawalStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminWalletService(
    private val walletService: WalletService,
    private val withdrawalRepository: WithdrawalRequestRepository,
    private val userRepository: UserRepository,
    private val walletRepository: com.kazemieh.shop.wallet.persistence.WalletRepository
) {

    @Transactional(readOnly = true)
    fun searchUsersWithWallet(query: String): List<UserWalletInfoResponse> {
        val users = if (query.isBlank()) {
            userRepository.findAll().take(20) // Limit for performance if empty
        } else {
            userRepository.findAll().filter {
                it.email?.contains(query, ignoreCase = true) == true ||
                "${it.firstName} ${it.lastName}".contains(query, ignoreCase = true)
            }
        }

        return users.map { user ->
            val wallet = walletRepository.findByUserId(user.id)
            UserWalletInfoResponse(
                userId = user.id,
                email = user.email,
                fullName = "${user.firstName ?: ""} ${user.lastName ?: ""}".trim().ifBlank { null },
                balance = wallet?.balance ?: java.math.BigDecimal.ZERO
            )
        }
    }

    @Transactional
    fun adjustBalance(req: AdminAdjustBalanceRequest) {
        walletService.addTransaction(
            userId = req.userId,
            amount = req.amount,
            type = TransactionType.ADJUSTMENT,
            description = req.description ?: "اصلاحیه دستی توسط ادمین",
            referenceId = null
        )
    }

    @Transactional(readOnly = true)
    fun listWithdrawalRequests(status: WithdrawalStatus?): List<WithdrawalRequestResponse> {
        val requests = if (status != null) {
            withdrawalRepository.findAllByStatusOrderByCreatedAtAsc(status)
        } else {
            withdrawalRepository.findAll()
        }
        
        return requests.map {
            WithdrawalRequestResponse(
                id = it.id,
                userId = it.user!!.id,
                userFullName = "${it.user!!.firstName ?: ""} ${it.user!!.lastName ?: ""}".trim().ifBlank { null },
                userEmail = it.user!!.email,
                amount = it.amount,
                iban = it.iban,
                status = it.status,
                adminNote = it.adminNote,
                createdAt = it.createdAt
            )
        }
    }

    @Transactional
    fun processWithdrawal(id: Long, req: AdminProcessWithdrawalRequest) {
        val withdrawal = withdrawalRepository.findById(id).orElseThrow { RuntimeException("Withdrawal request not found") }
        
        if (withdrawal.status == WithdrawalStatus.PAID || withdrawal.status == WithdrawalStatus.REJECTED) {
            throw RuntimeException("Withdrawal request already processed")
        }

        if (req.status == WithdrawalStatus.REJECTED) {
            // بازگرداندن وجه به کیف پول در صورت رد درخواست
            walletService.addTransaction(
                userId = withdrawal.user!!.id,
                amount = withdrawal.amount,
                type = TransactionType.REFUND,
                description = "بازگشت وجه - رد درخواست برداشت: ${req.adminNote ?: ""}",
                referenceId = withdrawal.id.toString()
            )
        }

        withdrawal.status = req.status
        withdrawal.adminNote = req.adminNote
        withdrawalRepository.save(withdrawal)
    }
}
