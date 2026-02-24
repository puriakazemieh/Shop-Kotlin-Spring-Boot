package com.kazemieh.shop.shared.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.kazemieh.shop.shared.error.ApiError
import com.kazemieh.shop.shared.error.ErrorCodes
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class RestAccessDeniedHandler(private val om: ObjectMapper) : AccessDeniedHandler {
    override fun handle(req: HttpServletRequest, res: HttpServletResponse, ex: AccessDeniedException) {
        res.status = HttpStatus.FORBIDDEN.value()
        res.contentType = "application/json"
        val body = ApiError(
            status = HttpStatus.FORBIDDEN,
            message = "Access denied",
            errorCode = ErrorCodes.ACCESS_DENIED,
            path = req.requestURI
        )
        res.writer.write(om.writeValueAsString(body))
    }
}