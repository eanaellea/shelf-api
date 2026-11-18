package dev.anaelleea.shelfapi.user

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@Tag(name = "Users", description = "Invited users, each with their own shelf")
class UserController(private val service: UserService) {

    /** Lets the front end find the signed-in user's handle, e.g. to build their public shelf link. */
    @GetMapping("/api/me")
    @Operation(summary = "Get the signed-in user")
    @SecurityRequirement(name = "firebase")
    fun me(principal: Principal) = service.getByFirebaseUid(principal.name).toResponse()

    /** Admin only: registers a Firebase account the admin has created, with the handle of its shelf. */
    @PostMapping("/api/admin/users")
    @Operation(summary = "Invite a user")
    @SecurityRequirement(name = "firebase")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateUserRequest) = service.create(request)

}
