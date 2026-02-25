package com.kazemieh.shop.identity.application.exception

import com.kazemieh.shop.shared.error.*
import org.springframework.http.HttpStatus

// ===== Users / Auth =====

class UserNotFoundException(message: String = "User not found")
    : NotFoundException(message, IdentityErrorCodes.USER_NOT_FOUND)

class EmailAlreadyExistsException(email: String)
    : ConflictException("Email already exists: $email", IdentityErrorCodes.EMAIL_ALREADY_EXISTS)

class UserInactiveException(message: String = "User is inactive")
    : ForbiddenException(message, IdentityErrorCodes.USER_INACTIVE)

class InvalidCredentialsException(message: String = "Invalid credentials")
    : UnauthorizedException(message, IdentityErrorCodes.INVALID_CREDENTIALS)

class ShopAccessDeniedException(message: String = "Access denied")
    : ForbiddenException(message, IdentityErrorCodes.ACCESS_DENIED)

class InvalidCurrentPasswordException :
    ApiException("Current password is invalid", "INVALID_CURRENT_PASSWORD", HttpStatus.BAD_REQUEST)

class SameAsOldPasswordException :
    ApiException("New password must be different", "SAME_AS_OLD_PASSWORD", HttpStatus.BAD_REQUEST)