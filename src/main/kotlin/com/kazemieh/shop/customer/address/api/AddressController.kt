package com.kazemieh.shop.customer.address.api

import com.kazemieh.shop.customer.address.api.dto.CreateAddressRequest
import com.kazemieh.shop.customer.address.api.dto.UpdateAddressRequest
import com.kazemieh.shop.customer.address.application.AddressService
import com.kazemieh.shop.shared.security.UserPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/addresses")
class AddressController(
    private val addressService: AddressService
) {

    @GetMapping
    fun list(@AuthenticationPrincipal principal: UserPrincipal) =
        addressService.list(principal.id)

    @GetMapping("/{id}")
    fun get(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long
    ) = addressService.get(principal.id, id)

    @GetMapping("/default")
    fun getDefault(@AuthenticationPrincipal principal: UserPrincipal) =
        addressService.getDefault(principal.id)

    @PostMapping
    fun create(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody req: CreateAddressRequest
    ) = addressService.create(principal.id, req)

    @PatchMapping("/{id}")
    fun update(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long,
        @Valid @RequestBody req: UpdateAddressRequest
    ) = addressService.update(principal.id, id, req)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long
    ) = addressService.delete(principal.id, id)


    @PostMapping("/{id}/default")
    fun setDefault(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long
    ) = addressService.setDefault(principal.id, id)
}