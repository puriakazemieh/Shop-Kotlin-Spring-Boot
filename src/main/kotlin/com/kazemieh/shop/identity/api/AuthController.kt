package com.kazemieh.shop.identity.api

import com.kazemieh.shop.identity.api.dto.*
import com.kazemieh.shop.identity.application.AuthService
import com.kazemieh.shop.shared.security.UserPrincipal
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/register")
    fun register(@Valid @RequestBody req: RegisterRequest) = authService.register(req)

    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequest) = authService.login(req)

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody req: RefreshRequest) = authService.refresh(req)

    @PostMapping("/logout")
    fun logout(@Valid @RequestBody req: LogoutRequest) = authService.logout(req)

    @PostMapping("/logout-all")
    fun logoutAll(@AuthenticationPrincipal principal: UserPrincipal) =
        authService.logoutAll(principal.id)

    @PostMapping("/forgot-password")
    fun forgotPassword(@Valid @RequestBody req: ForgotPasswordRequest) {
        authService.forgotPassword(req.email)
    }

    @PostMapping("/reset-password")
    fun resetPassword(@Valid @RequestBody req: ResetPasswordRequest) {
        authService.resetPassword(req)
    }
}
