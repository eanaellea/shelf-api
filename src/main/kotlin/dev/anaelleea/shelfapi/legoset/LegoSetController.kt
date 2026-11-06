package dev.anaelleea.shelfapi.legoset

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

@RestController
@RequestMapping("/api/lego-sets")
@Tag(name = "LEGO sets", description = "The sets on the shelf: owned, wishlisted and gifted")
class LegoSetController(private val service: LegoSetService) {

    @GetMapping
    @Operation(summary = "List the sets on the shelf, optionally filtered by status")
    fun list(
        @RequestParam status: LegoSetStatus?,
        @PageableDefault(size = 20, sort = ["addedAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ) = PagedModel(service.findAll(status, pageable))

    @GetMapping("/{setNumber}")
    @Operation(summary = "Get a set by its set number")
    fun get(@PathVariable setNumber: String) = service.find(setNumber)

    @PostMapping
    @Operation(summary = "Add a set to the shelf, with its details fetched from Rebrickable")
    @SecurityRequirement(name = "firebase")
    fun add(@Valid @RequestBody request: AddLegoSetRequest): ResponseEntity<LegoSetResponse> {
        val added = service.add(request)
        val location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{setNumber}").buildAndExpand(added.setNumber).toUri()
        return ResponseEntity.created(location).body(added)
    }

    @PatchMapping("/{setNumber}")
    @Operation(summary = "Update a set's status")
    @SecurityRequirement(name = "firebase")
    fun update(@PathVariable setNumber: String, @Valid @RequestBody request: UpdateLegoSetRequest) =
        service.update(setNumber, request)

    /** Public: lets a guest claim a wishlisted set as their gift, so nobody else buys it too. */
    @PostMapping("/{setNumber}/gift")
    @Operation(summary = "Mark a wishlisted set as gifted")
    fun gift(@PathVariable setNumber: String) = service.gift(setNumber)

    @DeleteMapping("/{setNumber}")
    @Operation(summary = "Remove a set from the shelf")
    @SecurityRequirement(name = "firebase")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable setNumber: String) = service.delete(setNumber)

}
