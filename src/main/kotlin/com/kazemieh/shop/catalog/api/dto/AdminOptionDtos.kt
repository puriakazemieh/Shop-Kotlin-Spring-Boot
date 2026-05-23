package com.kazemieh.shop.catalog.api.dto

import jakarta.validation.constraints.NotBlank

// Responses
data class AdminOptionGroupResponse(
    val id: Long,
    val name: String,
    val values: List<AdminOptionValueResponse>
)

data class AdminOptionTypeResponse(
    val id: Long,
    val name: String
)

data class AdminOptionValueResponse(
    val id: Long,
    val value: String
)

// Requests
data class AdminCreateOptionTypeRequest(
    @field:NotBlank val name: String
)

data class AdminUpdateOptionTypeRequest(
    @field:NotBlank val name: String
)

data class AdminCreateOptionValueRequest(
    val optionTypeId: Long,
    @field:NotBlank val value: String
)

data class AdminUpdateOptionValueRequest(
    @field:NotBlank val value: String
)
