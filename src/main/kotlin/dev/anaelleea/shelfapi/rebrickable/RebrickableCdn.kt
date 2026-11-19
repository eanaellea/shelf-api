package dev.anaelleea.shelfapi.rebrickable

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

/** Rebrickable's image CDN, which hosts the set images that [RebrickableSet.setImgUrl] links to. */
@HttpExchange(accept = ["image/*"])
interface RebrickableCdn {

    @GetExchange("/media/sets/{setNumber}/{file}")
    fun getSetImage(@PathVariable setNumber: String, @PathVariable file: String): ResponseEntity<ByteArray>

    /** Some sets, mostly older ones, have a single image named after the set, like `8880-1.jpg`. */
    @GetExchange("/media/sets/{file}")
    fun getFlatSetImage(@PathVariable file: String): ResponseEntity<ByteArray>

    /** The image at [getSetImage], at most [THUMBNAIL_SIZE] pixels: originals can be 2400 pixels wide and weigh megabytes. */
    @GetExchange("/media/thumbs/sets/{setNumber}/{file}/$THUMBNAIL_SIZE.jpg")
    fun getSetThumbnail(@PathVariable setNumber: String, @PathVariable file: String): ResponseEntity<ByteArray>

    /** The image at [getFlatSetImage], at most [THUMBNAIL_SIZE] pixels. */
    @GetExchange("/media/thumbs/sets/{file}/$THUMBNAIL_SIZE.jpg")
    fun getFlatSetThumbnail(@PathVariable file: String): ResponseEntity<ByteArray>

}

/** Big enough for the app's largest set image (160 logical pixels) on a 3x screen. */
const val THUMBNAIL_SIZE = "500x500p"
