package com.kazemieh.shop.identity.application

import com.kazemieh.shop.identity.api.dto.*
import com.kazemieh.shop.identity.api.mapper.UserMapper
import com.kazemieh.shop.identity.application.exception.*
import com.kazemieh.shop.identity.persistence.UserRepository
import com.kazemieh.shop.identity.persistence.entity.UserEntity
import com.kazemieh.shop.shared.EmailService
import com.kazemieh.shop.shared.security.jwt.JwtService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.*

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val refreshTokenService: RefreshTokenService,
    private val emailService: EmailService
) {

    @Transactional
    fun register(req: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(req.email)) throw EmailAlreadyExistsException(req.email)
        val hash = passwordEncoder.encode(req.password) ?: throw InvalidCredentialsException("Password is required")
        val saved = userRepository.save(
            UserEntity(
                email = req.email,
                passwordHash = hash
            )
        )

        val access = jwtService.generateAccessToken(saved.id, saved.email, saved.role.name)
        val refresh = refreshTokenService.issueFor(saved)
        return AuthResponse(access, refresh, UserMapper.toResponse(saved))
    }

    @Transactional
    fun login(req: LoginRequest): AuthResponse {
        try {
            authManager.authenticate(UsernamePasswordAuthenticationToken(req.email, req.password))
        } catch (_: BadCredentialsException) {
            throw InvalidCredentialsException()
        }

        val u = userRepository.findByEmail(req.email) ?: throw InvalidCredentialsException()
        if (!u.isActive) throw UserInactiveException()

        val access = jwtService.generateAccessToken(u.id, u.email, u.role.name)
        val refresh = refreshTokenService.issueFor(u)
        return AuthResponse(access, refresh, UserMapper.toResponse(u))
    }

    @Transactional
    fun refresh(req: RefreshRequest): AuthResponse {
        // validate old refresh (and revoke it)
        val oldUser = refreshTokenService.rotate(req.refreshToken)

        val u = userRepository.findById(oldUser.id).orElseThrow { InvalidCredentialsException("Invalid refresh token") }
        if (!u.isActive) throw UserInactiveException()

        val access = jwtService.generateAccessToken(u.id, u.email, u.role.name)
        val newRefresh = refreshTokenService.issueFor(u)
        return AuthResponse(access, newRefresh, UserMapper.toResponse(u))
    }

    @Transactional
    fun logout(req: LogoutRequest) {
        refreshTokenService.revoke(req.refreshToken)
    }

    @Transactional
    fun logoutAll(userId: Long) {
        refreshTokenService.revokeAllForUser(userId)
    }

    @Transactional
    fun forgotPassword(email: String) {
        val user = userRepository.findByEmail(email) ?: throw UserNotFoundException(email)

        val token = UUID.randomUUID().toString()
        user.resetPasswordToken = token
        user.resetPasswordTokenExpiry = OffsetDateTime.now().plusHours(1) // Token valid for 1 hour
        userRepository.save(user)

        val resetLink = "http://your-frontend-url/reset-password?token=$token"
        val message = "To reset your password, click the link: $resetLink"
        emailService.sendSimpleMessage(user.email, "Password Reset Request", message)
    }

    @Transactional
    fun resetPassword(req: ResetPasswordRequest) {
        val user = userRepository.findByResetPasswordToken(req.token)
            .orElseThrow { InvalidTokenException("Invalid token") }

        if (user.resetPasswordTokenExpiry?.isBefore(OffsetDateTime.now()) == true) {
            throw InvalidTokenException("Token has expired")
        }

        user.passwordHash = passwordEncoder.encode(req.newPassword)?:""
        user.resetPasswordToken = null
        user.resetPasswordTokenExpiry = null
        userRepository.save(user)
    }
}
