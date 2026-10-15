package dev.anaelleea.shelfapi.rebrickable

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.ErrorResponseException

// ErrorResponseException is rendered as problem details (application/problem+json) by Spring MVC.
class RebrickableUnavailableException(cause: Throwable) :
    ErrorResponseException(
        HttpStatus.BAD_GATEWAY,
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, "Rebrickable request failed"),
        cause,
    )
