package dev.anaelleea.shelfapi.legoset

import dev.anaelleea.shelfapi.user.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.data.web.PagedModel
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/** Public: each user's shelf is a gift registry that anyone can browse, found by the user's handle. */
@RestController
@RequestMapping("/api/shelves/{handle}/lego-sets")
@Tag(name = "Shelves", description = "Browse a user's shelf and gift sets from their wishlist")
class ShelfController(
    private val service: LegoSetService,
    private val users: UserService,
) {

    @GetMapping
    @Operation(summary = "List the sets on a shelf, optionally filtered by status")
    fun list(
        @PathVariable handle: String,
        @RequestParam status: LegoSetStatus?,
        @PageableDefault(size = 20, sort = ["addedAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ) = PagedModel(service.findAll(users.getByHandle(handle), status, pageable))

    @GetMapping("/{setNumber}")
    @Operation(summary = "Get a set on a shelf by its set number")
    fun get(@PathVariable handle: String, @PathVariable setNumber: String) =
        service.find(users.getByHandle(handle), setNumber)

    /** Lets a guest claim a wishlisted set as their gift, so nobody else buys it too. */
    @PostMapping("/{setNumber}/gift")
    @Operation(summary = "Mark a wishlisted set as gifted")
    fun gift(@PathVariable handle: String, @PathVariable setNumber: String) =
        service.gift(users.getByHandle(handle), setNumber)

}
