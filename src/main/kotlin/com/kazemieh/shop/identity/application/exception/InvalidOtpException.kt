package com.kazemieh.shop.identity.application.exception

import com.kazemieh.shop.shared.error.UnauthorizedException

class InvalidOtpException(message: String = "Invalid or expired OTP")
    : UnauthorizedException(message, IdentityErrorCodes.INVALID_OTP)
