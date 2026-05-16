package com.kazemieh.shop.catalog.api

import com.kazemieh.shop.catalog.api.dto.AdminCreateSizeRequest
import com.kazemieh.shop.catalog.api.dto.AdminUpdateSizeRequest
import com.kazemieh.shop.catalog.application.AdminCatalogService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/sizes")
@PreAuthorize("hasRole('ADMIN')")
class AdminSizeController(
    private val adminCatalogService: AdminCatalogService
) {

    @GetMapping
    fun list() = adminCatalogService.listSizes()

    @PostMapping
    fun create(@Valid @RequestBody req: AdminCreateSizeRequest) =
        adminCatalogService.createSize(req)

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody req: AdminUpdateSizeRequest) =
        adminCatalogService.updateSize(id, req)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = adminCatalogService.deleteSize(id)
}