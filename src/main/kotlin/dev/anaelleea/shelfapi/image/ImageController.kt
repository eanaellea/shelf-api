package dev.anaelleea.shelfapi.image

import dev.anaelleea.shelfapi.legoset.RebrickableUnavailableException
import dev.anaelleea.shelfapi.rebrickable.RebrickableCdn
import dev.anaelleea.shelfapi.rebrickable.THUMBNAIL_SIZE
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.ErrorResponseException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientException
import java.time.Duration

/**
 * Serves Rebrickable set images with this API's CORS headers. Rebrickable's CDN sends none,
 * so the Flutter web app can't draw its images itself.
 * Only set image paths are accepted, so this can't be used to fetch anything else.
 */
@RestController
@RequestMapping("/api/images")
@Tag(name = "Images", description = "Set images, served so that the web app can draw them")
class ImageController(
    private val cdn: RebrickableCdn,
) {

    @GetMapping("/rebrickable/sets/{setNumber:[\\w-]+}/{file:$IMAGE_FILE}")
    @Operation(summary = "Get a set image from Rebrickable's CDN (cdn.rebrickable.com/media/sets/{set}/{file})")
    fun rebrickableSetImage(@PathVariable setNumber: String, @PathVariable file: String) =
        serve { cdn.getSetImage(setNumber, file) }

    @GetMapping("/rebrickable/sets/{file:$IMAGE_FILE}")
    @Operation(summary = "Get a set image from Rebrickable's CDN (cdn.rebrickable.com/media/sets/{file})")
    fun rebrickableFlatSetImage(@PathVariable file: String) = serve { cdn.getFlatSetImage(file) }

    @GetMapping("/rebrickable/thumbs/sets/{setNumber:[\\w-]+}/{file:$IMAGE_FILE}")
    @Operation(summary = "Get a set image from Rebrickable's CDN, at most $THUMBNAIL_SIZE pixels")
    fun rebrickableSetThumbnail(@PathVariable setNumber: String, @PathVariable file: String) =
        serve { cdn.getSetThumbnail(setNumber, file) }

    @GetMapping("/rebrickable/thumbs/sets/{file:$IMAGE_FILE}")
    @Operation(summary = "Get a set image named after the set from Rebrickable's CDN, at most $THUMBNAIL_SIZE pixels")
    fun rebrickableFlatSetThumbnail(@PathVariable file: String) = serve { cdn.getFlatSetThumbnail(file) }

    private fun serve(fetch: () -> ResponseEntity<ByteArray>): ResponseEntity<ByteArray> {
        val image = try {
            fetch()
        } catch (_: HttpClientErrorException.NotFound) {
            throw ImageNotFoundException()
        } catch (e: RestClientException) {
            log.warn("Rebrickable image request failed: {}", e.message)
            throw RebrickableUnavailableException(e)
        }
        val contentType = image.headers.contentType?.takeIf { it.type == "image" }
            ?: throw RebrickableUnavailableException(IllegalStateException("Not an image: ${image.headers.contentType}"))

        return ResponseEntity.ok()
            // Thumbnails come as "image/jpeg; charset=utf-8", which means nothing for an image.
            .contentType(MediaType(contentType.type, contentType.subtype))
            // Rebrickable gives each image version its own URL, so it never changes.
            .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic().immutable())
            .body(image.body)
    }

    private companion object {
        const val IMAGE_FILE = "[\\w-]+\\.(?:jpg|jpeg|png)"
        val log: Logger = LoggerFactory.getLogger(ImageController::class.java)
    }

}

class ImageNotFoundException :
    ErrorResponseException(
        HttpStatus.NOT_FOUND,
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Image not found"),
        null,
    )
