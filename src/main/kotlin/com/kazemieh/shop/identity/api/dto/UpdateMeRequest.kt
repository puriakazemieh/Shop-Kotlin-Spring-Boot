package com.kazemieh.shop.identity.api.dto

import jakarta.validation.constraints.Size

data class UpdateMeRequest(

    @field:Size(min = 3, max = 50, message = "First name must be between 3 and 50 characters")
    val firstName: String?,

    @field:Size(min = 3, max = 50, message = "Last name must be between 3 and 50 characters")
    val lastName: String?,

    @field:Size(min = 5, max = 30, message = "Phone must be between 5 and 30 characters")
    val phone: String?,

    @field:Size(min = 3, max = 50, message = "City must be between 3 and 50 characters")
    val city: String?,

    val postalCode: Int?,

    )
