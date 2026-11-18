package dev.anaelleea.shelfapi.legoset

import dev.anaelleea.shelfapi.rebrickable.RebrickableApi
import dev.anaelleea.shelfapi.user.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientException
import java.time.Instant

@Service
@Transactional(readOnly = true)
class LegoSetService(
    private val legoSets: LegoSetRepository,
    private val themes: LegoThemeRepository,
    private val rebrickable: RebrickableApi,
) {

    fun findAll(user: User, status: LegoSetStatus?, pageable: Pageable): Page<LegoSetResponse> {
        val page = if (status == null) legoSets.findAllByUser(user, pageable) else legoSets.findAllByUserAndStatus(user, status, pageable)
        return page.map { it.toResponse() }
    }

    fun find(user: User, setNumber: String): LegoSetResponse = getOrThrow(user, normalize(setNumber)).toResponse()

    // Not transactional: Rebrickable calls shouldn't hold a database connection open.
    // Each repository save runs in its own transaction.
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun add(user: User, request: AddLegoSetRequest): LegoSetResponse {
        val setNumber = normalize(request.setNumber)
        if (legoSets.existsByUserAndSetNumber(user, setNumber)) throw LegoSetAlreadyExistsException(setNumber)

        val catalogSet = callRebrickable(setNumber) { rebrickable.getSet(setNumber) }
        val theme = themes.findByIdOrNull(catalogSet.themeId)
            ?: themes.save(callRebrickable(setNumber) { rebrickable.getTheme(catalogSet.themeId) }.let { LegoTheme(it.id, it.name) })

        val legoSet = LegoSet(
            user = user,
            setNumber = catalogSet.setNum,
            name = catalogSet.name,
            theme = theme,
            releaseYear = catalogSet.year.toShort(),
            pieceCount = catalogSet.numParts,
            imageUrl = catalogSet.setImgUrl,
            status = request.status,
        )
        return try {
            legoSets.saveAndFlush(legoSet).toResponse()
        } catch (e: DataIntegrityViolationException) {
            throw LegoSetAlreadyExistsException(setNumber) // added concurrently
        }
    }

    @Transactional
    fun update(user: User, setNumber: String, request: UpdateLegoSetRequest): LegoSetResponse {
        val legoSet = getOrThrow(user, normalize(setNumber))
        legoSet.status = request.status
        return legoSets.saveAndFlush(legoSet).toResponse()
    }

    /**
     * WISHLIST -> GIFTED in one conditional update, so when two guests gift the same set at once,
     * only the first succeeds and the second gets a conflict.
     */
    @Transactional
    fun gift(user: User, setNumber: String): LegoSetResponse {
        val normalized = normalize(setNumber)
        if (legoSets.updateStatusIf(user, normalized, from = LegoSetStatus.WISHLIST, to = LegoSetStatus.GIFTED, now = Instant.now()) == 0) {
            getOrThrow(user, normalized) // 404 when the set isn't on the shelf
            throw LegoSetNotOnWishlistException(normalized)
        }
        return getOrThrow(user, normalized).toResponse()
    }

    @Transactional
    fun delete(user: User, setNumber: String) {
        legoSets.delete(getOrThrow(user, normalize(setNumber)))
    }

    private fun getOrThrow(user: User, setNumber: String): LegoSet =
        legoSets.findByUserAndSetNumber(user, setNumber) ?: throw LegoSetNotFoundException(setNumber)

    private fun <T> callRebrickable(setNumber: String, call: () -> T): T =
        try {
            call()
        } catch (e: HttpClientErrorException.NotFound) {
            throw UnknownLegoSetException(setNumber)
        } catch (e: RestClientException) {
            log.warn("Rebrickable request for {} failed: {}", setNumber, e.message)
            throw RebrickableUnavailableException(e)
        }

    /** "10281" -> "10281-1": Rebrickable needs the variant suffix, and most sets only have variant 1. */
    private fun normalize(setNumber: String): String =
        setNumber.trim().let { if ('-' in it) it else "$it-1" }

    private companion object {
        val log: Logger = LoggerFactory.getLogger(LegoSetService::class.java)
    }

}
