package dev.anaelleea.shelfapi.legoset

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.ErrorResponseException

// ErrorResponseException is rendered as problem details (application/problem+json) by Spring MVC.

class LegoSetNotFoundException(setNumber: String) :
    ErrorResponseException(HttpStatus.NOT_FOUND, problem(HttpStatus.NOT_FOUND, "LEGO set $setNumber not found"), null)

class LegoSetAlreadyExistsException(setNumber: String) :
    ErrorResponseException(HttpStatus.CONFLICT, problem(HttpStatus.CONFLICT, "LEGO set $setNumber already exists"), null)

class UnknownLegoSetException(setNumber: String) :
    ErrorResponseException(
        HttpStatus.UNPROCESSABLE_CONTENT,
        problem(HttpStatus.UNPROCESSABLE_CONTENT, "Rebrickable has no LEGO set $setNumber"),
        null,
    )

class RebrickableUnavailableException(cause: Throwable) :
    ErrorResponseException(HttpStatus.BAD_GATEWAY, problem(HttpStatus.BAD_GATEWAY, "Rebrickable request failed"), cause)

private fun problem(status: HttpStatus, detail: String) = ProblemDetail.forStatusAndDetail(status, detail)
