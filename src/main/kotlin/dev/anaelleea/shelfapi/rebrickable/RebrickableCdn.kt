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

}
