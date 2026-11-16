package dev.anaelleea.shelfapi.catalog

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/catalog")
@Tag(name = "Catalog", description = "Search the Rebrickable catalog of LEGO sets")
class CatalogController(private val service: CatalogService) {

    @GetMapping("/sets")
    @Operation(summary = "Search Rebrickable for sets, newest first, flagging the ones already on the shelf")
    @SecurityRequirement(name = "firebase")
    fun searchSets(@RequestParam search: String) = service.search(search)

}
