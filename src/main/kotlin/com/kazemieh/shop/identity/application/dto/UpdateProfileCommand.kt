package com.kazemieh.shop.identity.application.dto

data class UpdateProfileCommand(
    val fullName: String?,
    val phone: String?
)