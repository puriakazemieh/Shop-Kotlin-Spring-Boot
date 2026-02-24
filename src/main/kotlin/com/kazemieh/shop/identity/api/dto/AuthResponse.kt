package com.kazemieh.shop.identity.api.dto


data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponse,
)