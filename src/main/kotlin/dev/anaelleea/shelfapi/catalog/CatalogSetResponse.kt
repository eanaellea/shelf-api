package dev.anaelleea.shelfapi.catalog

/** A set from the Rebrickable catalog, as returned by search. */
data class CatalogSetResponse(
    val setNumber: String,
    val name: String,
    /** Null when Rebrickable doesn't know the set's theme. */
    val theme: String?,
    val releaseYear: Int,
    val pieceCount: Int,
    /** Rebrickable CDN URL, or null when Rebrickable has no image. */
    val imageUrl: String?,
    /** Whether the set is already on the shelf. */
    val owned: Boolean,
)
