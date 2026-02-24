package com.kazemieh.shop.identity.application

import com.kazemieh.shop.identity.api.dto.UserResponse
import com.kazemieh.shop.identity.api.mapper.UserMapper
import com.kazemieh.shop.identity.application.dto.UpdateProfileCommand
import com.kazemieh.shop.identity.application.exception.UserNotFoundException
import com.kazemieh.shop.identity.persistence.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository
) {

    @Transactional(readOnly = true)
    fun getMe(userId: Long): UserResponse {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        return UserMapper.toResponse(user)
    }

    @Transactional
    fun updateMe(userId: Long, cmd: UpdateProfileCommand): UserResponse {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }

        user.fullName = cmd.fullName
        user.phone = cmd.phone

        return UserMapper.toResponse(user)
    }
}