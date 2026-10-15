package dev.anaelleea.shelfapi.catalog.controller

import dev.anaelleea.shelfapi.catalog.service.CatalogService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/catalog")
class CatalogController(private val service: CatalogService) {

    @GetMapping("/sets")
    fun searchSets(@RequestParam search: String) = service.search(search)

}
