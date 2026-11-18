package dev.anaelleea.shelfapi.user

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.ErrorResponseException

// ErrorResponseException is rendered as problem details (application/problem+json) by Spring MVC.

/** The request has a valid Firebase token, but the admin hasn't invited that account. */
class UserNotInvitedException :
    ErrorResponseException(HttpStatus.FORBIDDEN, problem(HttpStatus.FORBIDDEN, "This account hasn't been invited"), null)

class ShelfNotFoundException(handle: String) :
    ErrorResponseException(HttpStatus.NOT_FOUND, problem(HttpStatus.NOT_FOUND, "Shelf $handle not found"), null)

class UserAlreadyExistsException(firebaseUid: String) :
    ErrorResponseException(HttpStatus.CONFLICT, problem(HttpStatus.CONFLICT, "Firebase user $firebaseUid already exists"), null)

class HandleTakenException(handle: String) :
    ErrorResponseException(HttpStatus.CONFLICT, problem(HttpStatus.CONFLICT, "Handle $handle is already taken"), null)

private fun problem(status: HttpStatus, detail: String) = ProblemDetail.forStatusAndDetail(status, detail)
