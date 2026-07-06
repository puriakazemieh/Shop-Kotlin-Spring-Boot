package com.kazemieh.shop.identity.application

import com.kazemieh.shop.identity.api.dto.*
import com.kazemieh.shop.identity.api.mapper.UserMapper
import com.kazemieh.shop.identity.application.exception.*
import com.kazemieh.shop.identity.persistence.UserRepository
import com.kazemieh.shop.identity.persistence.entity.UserEntity
import com.kazemieh.shop.shared.EmailService
import com.kazemieh.shop.shared.SmsService
import com.kazemieh.shop.shared.security.jwt.JwtService
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.*
import kotlin.random.Random

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val refreshTokenService: RefreshTokenService,
    private val emailService: EmailService,
    private val smsService: SmsService,
    @Value("\${app.frontend-url:http://localhost:3000}") private val frontendUrl: String
) {

    @Transactional
    fun register(req: RegisterRequest): AuthResponse {
        if (req.email == null && req.mobile == null) {
            throw InvalidCredentialsException("Email or mobile is required")
        }

        req.email?.let {
            if (userRepository.existsByEmail(it)) throw EmailAlreadyExistsException(it)
        }
        req.mobile?.let {
            if (userRepository.existsByPhone(it)) throw MobileAlreadyExistsException(it)
        }

        val hash = passwordEncoder.encode(req.password) ?: throw InvalidCredentialsException("Password is required")
        val referrer = req.referralCode?.let { userRepository.findByReferralCode(it) }
        val saved = userRepository.save(
            UserEntity(
                email = req.email,
                phone = req.mobile,
                passwordHash = hash,
                referredByUserId = referrer?.id
            )
        )

        val username = saved.email ?: saved.phone!!
        val access = jwtService.generateAccessToken(saved.id, username, saved.role.name)
        val refresh = refreshTokenService.issueFor(saved)
        return AuthResponse(access, refresh, UserMapper.toResponse(saved))
    }

    @Transactional
    fun login(req: LoginRequest): AuthResponse {
        val user = userRepository.findByEmailOrPhone(req.username, req.username) ?: throw InvalidCredentialsException()

        try {
            authManager.authenticate(UsernamePasswordAuthenticationToken(req.username, req.password))
        } catch (_: BadCredentialsException) {
            throw InvalidCredentialsException()
        }

        if (!user.isActive) throw UserInactiveException()

        val username = user.email ?: user.phone!!
        val access = jwtService.generateAccessToken(user.id, username, user.role.name)
        val refresh = refreshTokenService.issueFor(user)
        return AuthResponse(access, refresh, UserMapper.toResponse(user))
    }

    @Transactional
    fun sendLoginOtp(req: SendLoginOtpRequest) {
        val user = userRepository.findByPhone(req.mobile) ?: throw UserNotFoundException(req.mobile)

        val otp = generateOtp()
        user.otpCode = otp
        user.otpExpiry = OffsetDateTime.now().plusMinutes(5) // OTP valid for 5 minutes
        userRepository.save(user)

        smsService.sendSms(user.phone!!, "Your login OTP code is: $otp")
    }

    @Transactional
    fun loginWithOtp(req: LoginWithOtpRequest): AuthResponse {
        val user = userRepository.findByPhone(req.mobile) ?: throw UserNotFoundException(req.mobile)

        if (user.otpCode != req.otpCode || user.otpExpiry?.isBefore(OffsetDateTime.now()) == true) {
            throw InvalidOtpException()
        }

        user.otpCode = null
        user.otpExpiry = null
        userRepository.save(user)

        if (!user.isActive) throw UserInactiveException()

        val username = user.email ?: user.phone!!
        val access = jwtService.generateAccessToken(user.id, username, user.role.name)
        val refresh = refreshTokenService.issueFor(user)
        return AuthResponse(access, refresh, UserMapper.toResponse(user))
    }

    @Transactional
    fun refresh(req: RefreshRequest): AuthResponse {
        // validate old refresh (and revoke it)
        val oldUser = refreshTokenService.rotate(req.refreshToken)

        val u = userRepository.findById(oldUser.id).orElseThrow { InvalidCredentialsException("Invalid refresh token") }
        if (!u.isActive) throw UserInactiveException()

        val username = u.email ?: u.phone!!
        val access = jwtService.generateAccessToken(u.id, username, u.role.name)
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
    fun forgotPassword(req: ForgotPasswordRequest) {
        if (req.email == null && req.mobile == null) {
            throw InvalidCredentialsException("Email or mobile is required")
        }

        val user = if (req.email != null) {
            userRepository.findByEmail(req.email) ?: throw UserNotFoundException(req.email)
        } else {
            userRepository.findByPhone(req.mobile!!) ?: throw UserNotFoundException(req.mobile)
        }

        if (user.phone != null) {
            val otp = generateOtp()
            user.otpCode = otp
            user.otpExpiry = OffsetDateTime.now().plusMinutes(5) // OTP valid for 5 minutes
            userRepository.save(user)
            smsService.sendSms(user.phone!!, "Your OTP code for password reset is: $otp")
        } else {
            val token = UUID.randomUUID().toString()
            user.resetPasswordToken = token
            user.resetPasswordTokenExpiry = OffsetDateTime.now().plusHours(1) // Token valid for 1 hour
            userRepository.save(user)

            val resetLink = "${frontendUrl.trimEnd('/')}/reset-password?token=$token"
            val message = "To reset your password, click the link: $resetLink"
            emailService.sendSimpleMessage(user.email!!, "Password Reset Request", message)
        }
    }

    @Transactional
    fun resetPassword(req: ResetPasswordRequest) {
        val user = userRepository.findByResetPasswordToken(req.token)
            .orElseThrow { InvalidTokenException("Invalid token") }

        if (user.resetPasswordTokenExpiry?.isBefore(OffsetDateTime.now()) == true) {
            throw InvalidTokenException("Token has expired")
        }

        user.passwordHash = passwordEncoder.encode(req.newPassword).toString()
        user.resetPasswordToken = null
        user.resetPasswordTokenExpiry = null
        userRepository.save(user)
    }

    @Transactional
    fun resetPasswordWithOtp(req: ResetPasswordWithOtpRequest) {
        val user = userRepository.findByPhone(req.mobile) ?: throw UserNotFoundException(req.mobile)

        if (user.otpCode != req.otpCode || user.otpExpiry?.isBefore(OffsetDateTime.now()) == true) {
            throw InvalidOtpException()
        }

        user.passwordHash = passwordEncoder.encode(req.newPassword).toString()
        user.otpCode = null
        user.otpExpiry = null
        userRepository.save(user)
    }

    private fun generateOtp(): String {
        // 6-digit random OTP (100000..999999)
        return Random.nextInt(100000, 1000000).toString()
    }
}
