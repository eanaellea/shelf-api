package dev.anaelleea.shelfapi.catalog

import dev.anaelleea.shelfapi.legoset.LegoSetRepository
import dev.anaelleea.shelfapi.legoset.LegoTheme
import dev.anaelleea.shelfapi.legoset.LegoThemeRepository
import dev.anaelleea.shelfapi.legoset.RebrickableUnavailableException
import dev.anaelleea.shelfapi.rebrickable.RebrickableApi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException

// Not transactional: Rebrickable calls shouldn't hold a database connection open.
@Service
class CatalogService(
    private val rebrickable: RebrickableApi,
    private val legoSets: LegoSetRepository,
    private val themes: LegoThemeRepository,
) {

    fun search(query: String): List<CatalogSetResponse> {
        if (query.isBlank()) return emptyList()

        val results = callRebrickable { rebrickable.searchSets(query.trim(), ordering = "-year", pageSize = SEARCH_PAGE_SIZE) }.results
        val themeNames = themeNames(results.map { it.themeId }.toSet())
        val owned = legoSets.findSetNumbersIn(results.map { it.setNum }).toSet()

        return results.map {
            CatalogSetResponse(
                setNumber = it.setNum,
                name = it.name,
                theme = themeNames[it.themeId],
                releaseYear = it.year,
                pieceCount = it.numParts,
                imageUrl = it.setImgUrl,
                owned = it.setNum in owned,
            )
        }
    }

    /**
     * Theme names by id, from the local cache. When one is missing, all Rebrickable themes are fetched
     * and the new ones cached: one request instead of one per theme.
     */
    private fun themeNames(ids: Set<Int>): Map<Int, String> {
        val cached = themes.findAllById(ids).associate { it.id to it.name }
        if (cached.keys.containsAll(ids)) return cached

        val all = callRebrickable { rebrickable.getThemes(pageSize = THEMES_PAGE_SIZE) }.results
        val known = themes.findAll().map { it.id }.toSet()
        themes.saveAll(all.filter { it.id !in known }.map { LegoTheme(it.id, it.name) })
        return all.associate { it.id to it.name }
    }

    private fun <T> callRebrickable(call: () -> T): T =
        try {
            call()
        } catch (e: RestClientException) {
            log.warn("Rebrickable catalog request failed: {}", e.message)
            throw RebrickableUnavailableException(e)
        }

    private companion object {
        const val SEARCH_PAGE_SIZE = 20
        const val THEMES_PAGE_SIZE = 1000 // Rebrickable has under 1000 themes
        val log: Logger = LoggerFactory.getLogger(CatalogService::class.java)
    }

}
