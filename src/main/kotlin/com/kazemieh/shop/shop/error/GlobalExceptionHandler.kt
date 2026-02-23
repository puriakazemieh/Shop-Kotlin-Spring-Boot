package com.kazemieh.shop.shop.error

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    private fun buildResponseEntity(status: HttpStatus, message: String): ResponseEntity<ApiError> {
        val error = ApiError(status = status, message = message)
        return ResponseEntity<ApiError>(error, status)
    }

    @ExceptionHandler(
        UserNotFoundException::class,
        ShoppingListNotFoundException::class,
        ShoppingListItemNotFoundException::class,
    )
    fun handleNotFoundException(exception: RuntimeException): ResponseEntity<ApiError> {
        return buildResponseEntity(HttpStatus.NOT_FOUND, exception.message.toString())
    }

    @ExceptionHandler(
        SignUpException::class,
        PasswordMismatchException::class
    )
    fun handleConflictException(exception: RuntimeException): ResponseEntity<ApiError> {
        return buildResponseEntity(HttpStatus.CONFLICT, exception.message.toString())
    }

    @ExceptionHandler(
        JwtAuthenticationException::class,
        TokenExpiredException::class,
        UsernamePasswordMismatchException::class,
        AccountVerificationException::class
    )
    fun handleUnauthorizedException(exception: RuntimeException): ResponseEntity<ApiError> {
        return buildResponseEntity(HttpStatus.UNAUTHORIZED, exception.message.toString())
    }

    @ExceptionHandler(
        BadRequestException::class,
    )
    fun handleBadRequestException(exception: BadRequestException): ResponseEntity<ApiError> {
        return buildResponseEntity(HttpStatus.BAD_REQUEST, exception.message.toString())
    }

}

class BadRequestException(message: String) : RuntimeException(message)
class SignUpException(message: String) : RuntimeException(message)
class JwtAuthenticationException(message: String, cause: Throwable? = null) : RuntimeException(message)
class UserNotFoundException(message: String) : RuntimeException(message)
class PasswordMismatchException(message: String) : RuntimeException(message)
class UsernamePasswordMismatchException(message: String) : RuntimeException(message)
class AccountVerificationException(message: String) : RuntimeException(message)
class TokenVerificationException(message: String) : RuntimeException(message)
class TokenExpiredException(message: String) : RuntimeException(message)
class ShoppingListNotFoundException(message: String) : IllegalStateException(message)
class ShoppingListItemNotFoundException(message: String) : IllegalStateException(message)



