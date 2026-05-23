package com.kazemieh.shop.catalog.api

import com.kazemieh.shop.catalog.api.dto.AdminCreateOptionTypeRequest
import com.kazemieh.shop.catalog.api.dto.AdminCreateOptionValueRequest
import com.kazemieh.shop.catalog.api.dto.AdminUpdateOptionTypeRequest
import com.kazemieh.shop.catalog.api.dto.AdminUpdateOptionValueRequest
import com.kazemieh.shop.catalog.application.AdminOptionService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/options")
class AdminOptionController(
    private val adminOptionService: AdminOptionService
) {

    @GetMapping
    fun listAllOptions() = adminOptionService.listAllOptions()

    // Option Types
    @PostMapping("/types")
    @ResponseStatus(HttpStatus.CREATED)
    fun createOptionType(@Valid @RequestBody req: AdminCreateOptionTypeRequest) =
        adminOptionService.createOptionType(req)

    @PutMapping("/types/{id}")
    fun updateOptionType(@PathVariable id: Long, @Valid @RequestBody req: AdminUpdateOptionTypeRequest) =
        adminOptionService.updateOptionType(id, req)

    @DeleteMapping("/types/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteOptionType(@PathVariable id: Long) = adminOptionService.deleteOptionType(id)

    // Option Values
    @PostMapping("/values")
    @ResponseStatus(HttpStatus.CREATED)
    fun createOptionValue(@Valid @RequestBody req: AdminCreateOptionValueRequest) =
        adminOptionService.createOptionValue(req)

    @PutMapping("/values/{id}")
    fun updateOptionValue(@PathVariable id: Long, @Valid @RequestBody req: AdminUpdateOptionValueRequest) =
        adminOptionService.updateOptionValue(id, req)

    @DeleteMapping("/values/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteOptionValue(@PathVariable id: Long) = adminOptionService.deleteOptionValue(id)
}
