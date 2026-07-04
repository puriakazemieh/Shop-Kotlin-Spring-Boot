package com.kazemieh.shop.catalog.api

import com.kazemieh.shop.catalog.api.dto.AdminBundleResponse
import com.kazemieh.shop.catalog.api.dto.AdminCreateBundleRequest
import com.kazemieh.shop.catalog.api.dto.AdminUpdateBundleRequest
import com.kazemieh.shop.catalog.application.AdminBundleService
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/bundles")
@PreAuthorize("hasRole('ADMIN')")
class AdminBundleController(
    private val adminBundleService: AdminBundleService
) {

    @GetMapping
    fun list(): List<AdminBundleResponse> = adminBundleService.list()

    @PostMapping
    fun create(@RequestBody req: AdminCreateBundleRequest): Map<String, Long> =
        mapOf("id" to adminBundleService.create(req))

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun update(@PathVariable id: Long, @RequestBody req: AdminUpdateBundleRequest) =
        adminBundleService.update(id, req)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = adminBundleService.delete(id)
}
