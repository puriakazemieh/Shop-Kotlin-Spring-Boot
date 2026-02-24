package com.kazemieh.shop.shared.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.kazemieh.shop.shared.error.ApiError
import com.kazemieh.shop.shared.error.ErrorCodes
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class RestAuthEntryPoint(private val om: ObjectMapper) : AuthenticationEntryPoint {
    override fun commence(req: HttpServletRequest, res: HttpServletResponse, ex: AuthenticationException) {
        res.status = HttpStatus.UNAUTHORIZED.value()
        res.contentType = "application/json"
        val body = ApiError(
            status = HttpStatus.UNAUTHORIZED,
            message = "Unauthorized",
            errorCode = ErrorCodes.INVALID_CREDENTIALS,
            path = req.requestURI
        )
        res.writer.write(om.writeValueAsString(body))
    }
}