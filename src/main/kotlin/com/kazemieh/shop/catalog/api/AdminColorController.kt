package com.kazemieh.shop.catalog.api

import com.kazemieh.shop.catalog.api.dto.AdminCreateColorRequest
import com.kazemieh.shop.catalog.api.dto.AdminUpdateColorRequest
import com.kazemieh.shop.catalog.application.AdminCatalogService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/colors")
@PreAuthorize("hasRole('ADMIN')")
class AdminColorController(
    private val adminCatalogService: AdminCatalogService
) {

    @GetMapping
    fun list() = adminCatalogService.listColors()

    @PostMapping
    fun create(@Valid @RequestBody req: AdminCreateColorRequest) =
        adminCatalogService.createColor(req)

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody req: AdminUpdateColorRequest) =
        adminCatalogService.updateColor(id, req)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = adminCatalogService.deleteColor(id)
}