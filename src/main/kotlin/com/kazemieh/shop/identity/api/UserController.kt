package com.kazemieh.shop.identity.api

import com.kazemieh.shop.identity.api.dto.UpdateProfileRequest
import com.kazemieh.shop.identity.application.UserService
import com.kazemieh.shop.identity.application.dto.UpdateProfileCommand
import com.kazemieh.shop.shared.security.UserPrincipal
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {

    @GetMapping("/me")
    fun me(@AuthenticationPrincipal principal: UserPrincipal) =
        userService.getMe(principal.id)

    @PatchMapping("/me")
    fun updateMe(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody req: UpdateProfileRequest
    ) =
        userService.updateMe(
            principal.id,
            UpdateProfileCommand(fullName = req.fullName, phone = req.phone)
        )
}