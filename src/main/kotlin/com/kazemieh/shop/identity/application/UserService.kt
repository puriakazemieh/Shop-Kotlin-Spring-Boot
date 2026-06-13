package com.kazemieh.shop.identity.application

import com.kazemieh.shop.identity.api.dto.ChangeEmailRequest
import com.kazemieh.shop.identity.api.dto.ChangePasswordRequest
import com.kazemieh.shop.identity.api.dto.UpdateMeRequest
import com.kazemieh.shop.identity.api.dto.UserResponse
import com.kazemieh.shop.identity.api.mapper.UserMapper
import com.kazemieh.shop.identity.application.exception.*
import com.kazemieh.shop.identity.persistence.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserMeService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val refreshTokenService: RefreshTokenService,
) {

    @Transactional(readOnly = true)
    fun getMe(userId: Long): UserResponse {
        val u = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        return UserMapper.toResponse(u)
    }

    @Transactional
    fun updateMe(userId: Long, req: UpdateMeRequest): UserResponse {
        val u = userRepository.findById(userId).orElseThrow { UserNotFoundException() }

        if (req.firstName != null) u.firstName = req.firstName
        if (req.lastName != null) u.lastName = req.lastName
        if (req.phone != null) u.phone = req.phone
        if (req.city != null) u.city = req.city
        if (req.postalCode != null) u.postalCode = req.postalCode

        val saved = userRepository.save(u)
        return UserMapper.toResponse(saved)
    }

    @Transactional
    fun changeEmail(userId: Long, req: ChangeEmailRequest): UserResponse {
        val u = userRepository.findById(userId).orElseThrow { UserNotFoundException() }

        val newEmail = req.email.trim().lowercase()
        if (newEmail != u.email?.lowercase() && u.email != null) {
            if (userRepository.existsByEmail(newEmail)) {
                throw EmailAlreadyExistsException(newEmail)
            }
            u.email = newEmail
        }

        val saved = userRepository.save(u)
        return UserMapper.toResponse(saved)
    }

    @Transactional
    fun changePassword(userId: Long, req: ChangePasswordRequest) {
        val u = userRepository.findById(userId).orElseThrow { UserNotFoundException() }

        val ok = passwordEncoder.matches(req.currentPassword, u.passwordHash)
        if (!ok) throw InvalidCurrentPasswordException()

        if (passwordEncoder.matches(req.newPassword, u.passwordHash)) {
            throw SameAsOldPasswordException()
        }

        u.passwordHash =
            passwordEncoder.encode(req.newPassword) ?: throw InvalidCredentialsException("Password is required")
        userRepository.save(u)

        refreshTokenService.revokeAllForUser(userId)
    }

    @Transactional
    fun deactivate(userId: Long) {
        val u = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        u.isActive = false
        userRepository.save(u)

        refreshTokenService.revokeAllForUser(userId)
    }
}