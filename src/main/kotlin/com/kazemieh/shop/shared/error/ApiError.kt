package com.kazemieh.shop.shared.error

import org.springframework.http.HttpStatus
import java.time.OffsetDateTime

data class ApiError(
    val message: String? = "Something API related went wrong",
    val status: HttpStatus = HttpStatus.BAD_REQUEST,
    val code: Int = status.value(),
    val errorCode: String? = null,
    val path: String? = null,
    val timestamp: OffsetDateTime = OffsetDateTime.now()
)