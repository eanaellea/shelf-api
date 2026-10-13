package dev.anaelleea.shelfapi.rebrickable

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.annotation.JsonNaming

/** Rebrickable API v3, see https://rebrickable.com/api/v3/docs/. */
@HttpExchange(accept = ["application/json"])
interface RebrickableApi {

    @GetExchange("/lego/sets/")
    fun searchSets(
        @RequestParam search: String,
        @RequestParam ordering: String,
        @RequestParam("page_size") pageSize: Int,
    ): RebrickablePage<RebrickableSet>

    @GetExchange("/lego/sets/{setNumber}/")
    fun getSet(@PathVariable setNumber: String): RebrickableSet

    @GetExchange("/lego/themes/")
    fun getThemes(@RequestParam("page_size") pageSize: Int): RebrickablePage<RebrickableTheme>

    @GetExchange("/lego/themes/{id}/")
    fun getTheme(@PathVariable id: Int): RebrickableTheme

}

data class RebrickablePage<T>(
    val results: List<T>,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RebrickableSet(
    val setNum: String,
    val name: String,
    val year: Int,
    val themeId: Int,
    val numParts: Int,
    val setImgUrl: String?,
)

data class RebrickableTheme(
    val id: Int,
    val name: String,
)
