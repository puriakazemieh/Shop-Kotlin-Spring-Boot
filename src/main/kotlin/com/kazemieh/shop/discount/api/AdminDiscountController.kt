package com.kazemieh.shop.discount.api

import com.kazemieh.shop.discount.api.dto.CreateDiscountRequest
import com.kazemieh.shop.discount.api.dto.DiscountResponse
import com.kazemieh.shop.discount.application.AdminDiscountService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/discounts")
@PreAuthorize("hasRole('ADMIN')")
class AdminDiscountController(
    private val adminDiscountService: AdminDiscountService
) {

    @PostMapping
    fun createDiscount(@RequestBody request: CreateDiscountRequest): ResponseEntity<DiscountResponse> {
        val discount = adminDiscountService.createDiscount(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(discount)
    }
}
