package com.kazemieh.shop.identity.api

import com.kazemieh.shop.identity.api.dto.ChangeEmailRequest
import com.kazemieh.shop.identity.api.dto.ChangePasswordRequest
import com.kazemieh.shop.identity.api.dto.UpdateMeRequest
import com.kazemieh.shop.identity.application.UserMeService
import com.kazemieh.shop.shared.security.UserPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users/me")
class UserMeController(
    private val userMeService: UserMeService,
) {

    @GetMapping
    fun me(@AuthenticationPrincipal principal: UserPrincipal) =
        userMeService.getMe(principal.id)

    @PatchMapping
    fun updateMe(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody req: UpdateMeRequest,
    ) = userMeService.updateMe(principal.id, req)

    @PatchMapping("/email")
    fun changeEmail(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody req: ChangeEmailRequest,
    ) = userMeService.changeEmail(principal.id, req)

    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun changePassword(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody req: ChangePasswordRequest,
    ) {
        userMeService.changePassword(principal.id, req)
    }

    @PostMapping("/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deactivate(@AuthenticationPrincipal principal: UserPrincipal) {
        userMeService.deactivate(principal.id)
    }
}