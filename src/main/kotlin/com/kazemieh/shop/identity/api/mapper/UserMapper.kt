package com.kazemieh.shop.identity.api.mapper


import com.kazemieh.shop.identity.api.dto.UserResponse
import com.kazemieh.shop.identity.persistence.entity.UserEntity

object UserMapper {
    fun toResponse(u: UserEntity) = UserResponse(
        id = u.id,
        email = u.email,
        fullName = u.fullName,
        phone = u.phone,
        role = u.role.name,
        isActive = u.isActive,
        createdAt = u.createdAt,
        updatedAt = u.updatedAt
    )
}