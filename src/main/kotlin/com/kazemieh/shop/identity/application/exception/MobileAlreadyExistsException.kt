package com.kazemieh.shop.identity.application.exception

import com.kazemieh.shop.shared.error.ConflictException

class MobileAlreadyExistsException(mobile: String)
    : ConflictException("Mobile already exists: $mobile", IdentityErrorCodes.MOBILE_ALREADY_EXISTS)
