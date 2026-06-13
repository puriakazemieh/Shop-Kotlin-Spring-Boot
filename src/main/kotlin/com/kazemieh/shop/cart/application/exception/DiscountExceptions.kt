package com.kazemieh.shop.cart.application.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class DiscountNotFoundException(code: String) : RuntimeException("Discount code '$code' not found or inactive")

@ResponseStatus(HttpStatus.BAD_REQUEST)
class DiscountExpiredException(code: String) : RuntimeException("Discount code '$code' has expired")

@ResponseStatus(HttpStatus.BAD_REQUEST)
class DiscountNotStartedException(code: String) : RuntimeException("Discount code '$code' is not yet active")

@ResponseStatus(HttpStatus.BAD_REQUEST)
class DiscountUsageLimitExceededException(code: String) : RuntimeException("Discount code '$code' usage limit exceeded")

@ResponseStatus(HttpStatus.BAD_REQUEST)
class DiscountMinOrderAmountNotMetException(code: String) : RuntimeException("Minimum order amount not met for discount code '$code'")
