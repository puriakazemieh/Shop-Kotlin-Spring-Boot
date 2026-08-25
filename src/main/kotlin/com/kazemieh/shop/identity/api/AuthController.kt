package com.kazemieh.shop.identity.api

import com.kazemieh.shop.identity.api.dto.*
import com.kazemieh.shop.identity.application.AuthService
import com.kazemieh.shop.identity.application.exception.InvalidCredentialsException
import com.kazemieh.shop.shared.security.UserPrincipal
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val webSessionCookieFactory: WebSessionCookieFactory,
) {
    @PostMapping("/register")
    fun register(@Valid @RequestBody req: RegisterRequest) = authService.register(req)

    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequest) = authService.login(req)

    @PostMapping("/send-login-otp")
    fun sendLoginOtp(@Valid @RequestBody req: SendLoginOtpRequest) {
        authService.sendLoginOtp(req)
    }

    @PostMapping("/login-with-otp")
    fun loginWithOtp(@Valid @RequestBody req: LoginWithOtpRequest) = authService.loginWithOtp(req)

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody req: RefreshRequest) = authService.refresh(req)

    @PostMapping("/logout")
    fun logout(@Valid @RequestBody req: LogoutRequest) = authService.logout(req)

    @PostMapping("/web/register")
    fun webRegister(@Valid @RequestBody req: RegisterRequest, response: HttpServletResponse) =
        webAuthResponse(authService.register(req), response)

    @PostMapping("/web/login")
    fun webLogin(@Valid @RequestBody req: LoginRequest, response: HttpServletResponse) =
        webAuthResponse(authService.login(req), response)

    @PostMapping("/web/login-with-otp")
    fun webLoginWithOtp(@Valid @RequestBody req: LoginWithOtpRequest, response: HttpServletResponse) =
        webAuthResponse(authService.loginWithOtp(req), response)

    @PostMapping("/web/refresh")
    fun webRefresh(
        @CookieValue(WebSessionCookieFactory.COOKIE_NAME, required = false) refreshToken: String?,
        response: HttpServletResponse,
    ) = webAuthResponse(authService.refresh(RefreshRequest(requireRefreshToken(refreshToken))), response)

    @PostMapping("/web/logout")
    fun webLogout(
        @CookieValue(WebSessionCookieFactory.COOKIE_NAME, required = false) refreshToken: String?,
        response: HttpServletResponse,
    ) {
        refreshToken?.takeIf { it.isNotBlank() }?.let { authService.logout(LogoutRequest(it)) }
        response.addHeader("Set-Cookie", webSessionCookieFactory.clear().toString())
    }

    @PostMapping("/logout-all")
    fun logoutAll(@AuthenticationPrincipal principal: UserPrincipal) =
        authService.logoutAll(principal.id)

    @PostMapping("/forgot-password")
    fun forgotPassword(@Valid @RequestBody req: ForgotPasswordRequest) {
        authService.forgotPassword(req)
    }

    @PostMapping("/reset-password")
    fun resetPassword(@Valid @RequestBody req: ResetPasswordRequest) {
        authService.resetPassword(req)
    }

    @PostMapping("/reset-password-with-otp")
    fun resetPasswordWithOtp(@Valid @RequestBody req: ResetPasswordWithOtpRequest) {
        authService.resetPasswordWithOtp(req)
    }

    private fun webAuthResponse(auth: AuthResponse, response: HttpServletResponse): WebAuthResponse {
        response.addHeader("Set-Cookie", webSessionCookieFactory.refreshToken(auth.refreshToken).toString())
        return WebAuthResponse(accessToken = auth.accessToken, user = auth.user)
    }

    private fun requireRefreshToken(value: String?): String =
        value?.takeIf { it.isNotBlank() } ?: throw InvalidCredentialsException("Missing web session")
}
