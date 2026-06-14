package com.kazemieh.shop.wallet.api.controller

import com.kazemieh.shop.payment.application.PaymentService
import com.kazemieh.shop.shared.security.UserPrincipal
import com.kazemieh.shop.wallet.api.dto.*
import com.kazemieh.shop.wallet.application.WalletService
import com.kazemieh.shop.wallet.persistence.entity.TransactionType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/wallet")
@Tag(name = "Wallet")
@SecurityRequirement(name = "bearerAuth")
class WalletController(
    private val walletService: WalletService,
    private val paymentService: PaymentService
) {

    @GetMapping("/balance")
    fun getBalance(@AuthenticationPrincipal principal: UserPrincipal): WalletResponse =
        walletService.getBalance(principal.id)

    @GetMapping("/transactions")
    fun getTransactions(@AuthenticationPrincipal principal: UserPrincipal, pageable: Pageable): Page<WalletTransactionResponse> =
        walletService.getTransactions(principal.id, pageable)

    @PostMapping("/top-up")
    fun topUp(@AuthenticationPrincipal principal: UserPrincipal, @RequestBody req: TopUpRequest): String? {
        val tx = walletService.createPendingTransaction(
            userId = principal.id,
            amount = req.amount,
            type = TransactionType.DEPOSIT,
            description = "شارژ کیف پول (در انتظار پرداخت)"
        )
        
        return paymentService.startPayment(
            orderId = null,
            amount = req.amount,
            userId = principal.id,
            walletTransactionId = tx.id
        )
    }

    @PostMapping("/withdraw")
    fun withdraw(@AuthenticationPrincipal principal: UserPrincipal, @RequestBody req: WithdrawalRequest): WithdrawalRequestResponse =
        walletService.requestWithdrawal(principal.id, req)
}
