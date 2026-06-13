package com.kazemieh.shop.discount.api

import com.kazemieh.shop.discount.api.dto.CreateDiscountRequest
import com.kazemieh.shop.discount.api.dto.DiscountResponse
import com.kazemieh.shop.discount.api.dto.UpdateDiscountRequest
import com.kazemieh.shop.discount.application.AdminDiscountService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/discounts")
@PreAuthorize("hasRole('ADMIN')")
class AdminDiscountController(
    private val adminDiscountService: AdminDiscountService
) {

    @GetMapping
    fun getAllDiscounts(): List<DiscountResponse> {
        return adminDiscountService.getAllDiscounts()
    }

    @GetMapping("/{id}")
    fun getDiscountById(@PathVariable id: Long): DiscountResponse {
        return adminDiscountService.getDiscountById(id)
    }

    @PostMapping
    fun createDiscount(@RequestBody request: CreateDiscountRequest): ResponseEntity<DiscountResponse> {
        val discount = adminDiscountService.createDiscount(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(discount)
    }

    @PutMapping("/{id}")
    fun updateDiscount(
        @PathVariable id: Long,
        @RequestBody request: UpdateDiscountRequest
    ): DiscountResponse {
        return adminDiscountService.updateDiscount(id, request)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteDiscount(@PathVariable id: Long) {
        adminDiscountService.deleteDiscount(id)
    }
}
