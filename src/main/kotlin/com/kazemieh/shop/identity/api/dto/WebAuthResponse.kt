package com.kazemieh.shop.identity.api.dto

data class WebAuthResponse(
    val accessToken: String,
    val user: UserResponse,
)
