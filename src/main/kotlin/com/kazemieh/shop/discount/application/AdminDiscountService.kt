package com.kazemieh.shop.discount.application

import com.kazemieh.shop.discount.api.dto.CreateDiscountRequest
import com.kazemieh.shop.discount.api.dto.DiscountResponse
import com.kazemieh.shop.discount.persistence.DiscountRepository
import com.kazemieh.shop.discount.persistence.entity.DiscountEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminDiscountService(
    private val discountRepository: DiscountRepository
) {

    @Transactional
    fun createDiscount(request: CreateDiscountRequest): DiscountResponse {
        val discount = DiscountEntity(
            code = request.code,
            type = request.type,
            value = request.value,
            maxDiscountAmount = request.maxDiscountAmount,
            minOrderAmount = request.minOrderAmount,
            startDate = request.startDate,
            endDate = request.endDate,
            usageLimit = request.usageLimit,
            isActive = request.isActive
        )
        val saved = discountRepository.save(discount)
        return toDiscountResponse(saved)
    }

    private fun toDiscountResponse(entity: DiscountEntity): DiscountResponse {
        return DiscountResponse(
            id = entity.id,
            code = entity.code,
            type = entity.type,
            value = entity.value,
            maxDiscountAmount = entity.maxDiscountAmount,
            minOrderAmount = entity.minOrderAmount,
            startDate = entity.startDate,
            endDate = entity.endDate,
            usageLimit = entity.usageLimit,
            usageCount = entity.usageCount,
            isActive = entity.isActive
        )
    }
}
