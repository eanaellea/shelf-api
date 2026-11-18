package dev.anaelleea.shelfapi.catalog

import dev.anaelleea.shelfapi.user.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/api/catalog")
@Tag(name = "Catalog", description = "Search the Rebrickable catalog of LEGO sets")
class CatalogController(
    private val service: CatalogService,
    private val users: UserService,
) {

    @GetMapping("/sets")
    @Operation(summary = "Search Rebrickable for sets, newest first, flagging the ones already on my shelf")
    @SecurityRequirement(name = "firebase")
    fun searchSets(principal: Principal, @RequestParam search: String) =
        service.search(users.getByFirebaseUid(principal.name), search)

}
