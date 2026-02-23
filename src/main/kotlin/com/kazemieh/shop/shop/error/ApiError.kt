package com.kazemieh.shop.shop.error

import org.springframework.http.HttpStatus
import java.time.LocalDateTime
import kotlin.time.ExperimentalTime

data class ApiError @OptIn(ExperimentalTime::class) constructor(
    val message: String? = "Something API related went wrong",
    val status: HttpStatus = HttpStatus.BAD_REQUEST,
    val code: Int = status.value(),
    val errorCode: String? = null,
    val path: String? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)