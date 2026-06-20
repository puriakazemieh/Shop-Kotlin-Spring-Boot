package com.kazemieh.shop.wallet.application

import com.kazemieh.shop.identity.persistence.UserRepository
import com.kazemieh.shop.wallet.api.dto.*
import com.kazemieh.shop.wallet.persistence.WalletRepository
import com.kazemieh.shop.wallet.persistence.WalletTransactionRepository
import com.kazemieh.shop.wallet.persistence.WithdrawalRequestRepository
import com.kazemieh.shop.wallet.application.exception.InsufficientBalanceException
import com.kazemieh.shop.wallet.persistence.entity.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class WalletService(
    private val walletRepository: WalletRepository,
    private val transactionRepository: WalletTransactionRepository,
    private val withdrawalRepository: WithdrawalRequestRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun getOrCreateWallet(userId: Long): WalletEntity {
        return walletRepository.findByUserId(userId) ?: run {
            val user = userRepository.findById(userId).orElseThrow { RuntimeException("User not found") }
            walletRepository.save(WalletEntity(user = user, balance = BigDecimal.ZERO))
        }
    }

    @Transactional
    fun getBalance(userId: Long): WalletResponse {
        val wallet = getOrCreateWallet(userId)
        return WalletResponse(balance = wallet.balance, userId = userId)
    }

    @Transactional
    fun getTransactions(userId: Long, pageable: Pageable): Page<WalletTransactionResponse> {
        val wallet = getOrCreateWallet(userId)
        return transactionRepository.findAllByWalletIdOrderByCreatedAtDesc(wallet.id, pageable).map {
            WalletTransactionResponse(
                id = it.id,
                amount = it.amount,
                type = it.type,
                description = it.description,
                referenceId = it.referenceId,
                createdAt = it.createdAt
            )
        }
    }

    @Transactional
    fun requestWithdrawal(userId: Long, req: WithdrawalRequest): WithdrawalRequestResponse {
        val wallet = getOrCreateWallet(userId)
        if (wallet.balance < req.amount) {
            throw InsufficientBalanceException()
        }

        // کسر موجودی بلافاصله هنگام درخواست برای جلوگیری از خرج کردن چندباره
        wallet.balance -= req.amount
        
        val withdrawal = WithdrawalRequestEntity(
            user = wallet.user,
            amount = req.amount,
            iban = req.iban,
            status = WithdrawalStatus.PENDING
        )
        
        val saved = withdrawalRepository.save(withdrawal)
        
        // ثبت تراکنش کسر وجه
        transactionRepository.save(
            WalletTransactionEntity(
                wallet = wallet,
                amount = req.amount.negate(),
                type = TransactionType.WITHDRAWAL,
                description = "درخواست برداشت وجه - شبا: ${req.iban}",
                referenceId = saved.id.toString()
            )
        )

        return WithdrawalRequestResponse(
            id = saved.id,
            userId = saved.user!!.id,
            userFullName = "${saved.user!!.firstName ?: ""} ${saved.user!!.lastName ?: ""}".trim().ifBlank { null },
            userEmail = saved.user!!.email,
            amount = saved.amount,
            iban = saved.iban,
            status = saved.status,
            adminNote = saved.adminNote,
            createdAt = saved.createdAt
        )
    }

    @Transactional
    fun createPendingTransaction(userId: Long, amount: BigDecimal, type: TransactionType, description: String?): WalletTransactionEntity {
        val wallet = getOrCreateWallet(userId)
        return transactionRepository.save(
            WalletTransactionEntity(
                wallet = wallet,
                amount = amount,
                type = type,
                description = description,
                referenceId = null
            )
        )
    }

    @Transactional
    fun confirmTransaction(transactionId: Long, referenceId: String) {
        val tx = transactionRepository.findById(transactionId).orElseThrow { RuntimeException("Transaction not found") }
        val wallet = tx.wallet!!
        wallet.balance += tx.amount
        tx.referenceId = referenceId
        transactionRepository.save(tx)
    }

    @Transactional
    fun addTransaction(userId: Long, amount: BigDecimal, type: TransactionType, description: String?, referenceId: String?): WalletTransactionEntity {
        val wallet = getOrCreateWallet(userId)
        wallet.balance += amount
        
        if (wallet.balance < BigDecimal.ZERO) {
            throw RuntimeException("Insufficient wallet balance for this operation")
        }

        return transactionRepository.save(
            WalletTransactionEntity(
                wallet = wallet,
                amount = amount,
                type = type,
                description = description,
                referenceId = referenceId
            )
        )
    }
}
