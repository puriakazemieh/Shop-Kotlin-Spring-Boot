package com.kazemieh.shop.identity.api

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class WebSessionCookieFactory(
    @Value("\${jwt.refresh-ttl-days:30}") private val refreshTtlDays: Long,
    @Value("\${app.web-session.cookie-secure:true}") private val secure: Boolean,
) {
    fun refreshToken(value: String): ResponseCookie = ResponseCookie.from(COOKIE_NAME, value)
        .httpOnly(true)
        .secure(secure)
        .sameSite("Strict")
        .path("/")
        .maxAge(Duration.ofDays(refreshTtlDays))
        .build()

    fun clear(): ResponseCookie = ResponseCookie.from(COOKIE_NAME, "")
        .httpOnly(true)
        .secure(secure)
        .sameSite("Strict")
        .path("/")
        .maxAge(Duration.ZERO)
        .build()

    companion object {
        const val COOKIE_NAME = "__Host-shop_refresh"
    }
}
