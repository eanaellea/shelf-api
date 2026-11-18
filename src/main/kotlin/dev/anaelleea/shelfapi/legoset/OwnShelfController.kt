package dev.anaelleea.shelfapi.legoset

import dev.anaelleea.shelfapi.user.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.data.web.PagedModel
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.security.Principal

/** The signed-in user's own shelf. principal.name is the Firebase token's subject: the user's Firebase UID. */
@RestController
@RequestMapping("/api/me/lego-sets")
@Tag(name = "My shelf", description = "Manage the signed-in user's sets: owned, wishlisted and gifted")
@SecurityRequirement(name = "firebase")
class OwnShelfController(
    private val service: LegoSetService,
    private val users: UserService,
) {

    @GetMapping
    @Operation(summary = "List my sets, optionally filtered by status")
    fun list(
        principal: Principal,
        @RequestParam status: LegoSetStatus?,
        @PageableDefault(size = 20, sort = ["addedAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ) = PagedModel(service.findAll(users.getByFirebaseUid(principal.name), status, pageable))

    @GetMapping("/{setNumber}")
    @Operation(summary = "Get one of my sets by its set number")
    fun get(principal: Principal, @PathVariable setNumber: String) =
        service.find(users.getByFirebaseUid(principal.name), setNumber)

    @PostMapping
    @Operation(summary = "Add a set to my shelf, with its details fetched from Rebrickable")
    fun add(principal: Principal, @Valid @RequestBody request: AddLegoSetRequest): ResponseEntity<LegoSetResponse> {
        val added = service.add(users.getByFirebaseUid(principal.name), request)
        val location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{setNumber}").buildAndExpand(added.setNumber).toUri()
        return ResponseEntity.created(location).body(added)
    }

    @PatchMapping("/{setNumber}")
    @Operation(summary = "Update one of my sets' status")
    fun update(
        principal: Principal,
        @PathVariable setNumber: String,
        @Valid @RequestBody request: UpdateLegoSetRequest,
    ) = service.update(users.getByFirebaseUid(principal.name), setNumber, request)

    @DeleteMapping("/{setNumber}")
    @Operation(summary = "Remove a set from my shelf")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(principal: Principal, @PathVariable setNumber: String) =
        service.delete(users.getByFirebaseUid(principal.name), setNumber)

}
