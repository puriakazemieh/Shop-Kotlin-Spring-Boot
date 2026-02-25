package com.kazemieh.shop.catalog.api

import com.kazemieh.shop.catalog.api.dto.AdminCreateCategoryRequest
import com.kazemieh.shop.catalog.api.dto.AdminUpdateCategoryRequest
import com.kazemieh.shop.catalog.application.AdminCatalogService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
class AdminCategoryController(
    private val adminCatalogService: AdminCatalogService
) {

    @GetMapping
    fun list() = adminCatalogService.listCategories()

    @PostMapping
    fun create(@Valid @RequestBody req: AdminCreateCategoryRequest) =
        adminCatalogService.createCategory(req)

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody req: AdminUpdateCategoryRequest) =
        adminCatalogService.updateCategory(id, req)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        adminCatalogService.deleteCategory(id)
    }
}