package com.kazemieh.shop.discount.application

import com.kazemieh.shop.discount.api.dto.CreateDiscountRequest
import com.kazemieh.shop.discount.api.dto.DiscountResponse
import com.kazemieh.shop.discount.api.dto.UpdateDiscountRequest
import com.kazemieh.shop.discount.persistence.DiscountRepository
import com.kazemieh.shop.discount.persistence.entity.DiscountEntity
import com.kazemieh.shop.cart.application.exception.DiscountIdNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminDiscountService(
    private val discountRepository: DiscountRepository
) {

    @Transactional(readOnly = true)
    fun getAllDiscounts(): List<DiscountResponse> {
        return discountRepository.findAll().map { toDiscountResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getDiscountById(id: Long): DiscountResponse {
        val discount = discountRepository.findById(id)
            .orElseThrow { DiscountIdNotFoundException(id) }
        return toDiscountResponse(discount)
    }

    @Transactional
    fun createDiscount(request: CreateDiscountRequest): DiscountResponse {
// ... existing createDiscount ...
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

    @Transactional
    fun updateDiscount(id: Long, request: UpdateDiscountRequest): DiscountResponse {
        val discount = discountRepository.findById(id)
            .orElseThrow { DiscountIdNotFoundException(id) }

        request.code?.let { discount.code = it }
        request.type?.let { discount.type = it }
        request.value?.let { discount.value = it }
        request.maxDiscountAmount?.let { discount.maxDiscountAmount = it }
        request.minOrderAmount?.let { discount.minOrderAmount = it }
        request.startDate?.let { discount.startDate = it }
        request.endDate?.let { discount.endDate = it }
        request.usageLimit?.let { discount.usageLimit = it }
        request.isActive?.let { discount.isActive = it }

        val updated = discountRepository.save(discount)
        return toDiscountResponse(updated)
    }

    @Transactional
    fun deleteDiscount(id: Long) {
        if (!discountRepository.existsById(id)) {
            throw DiscountIdNotFoundException(id)
        }
        discountRepository.deleteById(id)
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
