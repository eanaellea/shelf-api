package dev.anaelleea.shelfapi.user

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CreateUserRequest(
    /** UID of the Firebase account the admin created for this user. */
    @field:NotBlank
    @field:Size(max = 128)
    val firebaseUid: String,

    @field:Size(max = 30)
    @field:Pattern(regexp = HANDLE_PATTERN, message = "must be lowercase letters and digits, optionally separated by dashes, like anaelle or my-shelf")
    val handle: String,
)

data class UserResponse(
    val firebaseUid: String,
    val handle: String,
)

fun User.toResponse() = UserResponse(
    firebaseUid = firebaseUid,
    handle = handle,
)

// Lowercase words separated by single dashes, so handles are safe in URLs.
const val HANDLE_PATTERN = "^[a-z0-9]+(-[a-z0-9]+)*$"
