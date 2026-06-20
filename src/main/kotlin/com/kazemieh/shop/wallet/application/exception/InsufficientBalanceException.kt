package com.kazemieh.shop.wallet.application.exception

import com.kazemieh.shop.shared.error.ApiException
import com.kazemieh.shop.shared.error.ErrorCodes
import org.springframework.http.HttpStatus

class InsufficientBalanceException : ApiException(
    message = "Insufficient wallet balance",
    errorCode = ErrorCodes.INSUFFICIENT_WALLET_BALANCE,
    httpStatus = HttpStatus.BAD_REQUEST
)
