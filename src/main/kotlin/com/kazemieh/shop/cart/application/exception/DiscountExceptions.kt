package com.kazemieh.shop.cart.application.exception

import com.kazemieh.shop.shared.error.BadRequestException
import com.kazemieh.shop.shared.error.ErrorCodes
import com.kazemieh.shop.shared.error.NotFoundException

class DiscountNotFoundException(code: String) :
    NotFoundException("Discount code '$code' not found or inactive", ErrorCodes.DISCOUNT_NOT_FOUND)

class DiscountIdNotFoundException(id: Long) :
    NotFoundException("Discount with ID '$id' not found", ErrorCodes.DISCOUNT_NOT_FOUND)

class DiscountExpiredException(code: String) :
    BadRequestException("Discount code '$code' has expired", ErrorCodes.DISCOUNT_EXPIRED)

class DiscountNotStartedException(code: String) :
    BadRequestException("Discount code '$code' is not yet active", ErrorCodes.DISCOUNT_NOT_ACTIVE)

class DiscountUsageLimitExceededException(code: String) :
    BadRequestException("Discount code '$code' usage limit exceeded", ErrorCodes.DISCOUNT_LIMIT_EXCEEDED)

class DiscountMinOrderAmountNotMetException(code: String) :
    BadRequestException("Minimum order amount not met for discount code '$code'", ErrorCodes.DISCOUNT_MIN_AMOUNT_NOT_MET)

class DiscountCodeRequiredException :
    BadRequestException("Discount code is required", ErrorCodes.INVALID_INPUT)
