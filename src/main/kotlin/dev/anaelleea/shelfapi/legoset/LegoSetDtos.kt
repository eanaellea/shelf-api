package dev.anaelleea.shelfapi.legoset

import jakarta.validation.constraints.Pattern
import java.time.Instant

data class AddLegoSetRequest(
    @field:Pattern(regexp = SET_NUMBER_PATTERN, message = "must look like 10281 or 10281-1")
    val setNumber: String,
    val status: LegoSetStatus,
)

data class UpdateLegoSetRequest(
    val status: LegoSetStatus,
)

data class LegoSetResponse(
    val setNumber: String,
    val name: String,
    val theme: String,
    val releaseYear: Short,
    val pieceCount: Int,
    val status: LegoSetStatus,
    /** Rebrickable CDN URL, or null when Rebrickable has no image. */
    val imageUrl: String?,
    val addedAt: Instant,
    val updatedAt: Instant,
)

fun LegoSet.toResponse() = LegoSetResponse(
    setNumber = setNumber,
    name = name,
    theme = theme.name,
    releaseYear = releaseYear,
    pieceCount = pieceCount,
    status = status,
    imageUrl = imageUrl,
    addedAt = addedAt!!,
    updatedAt = updatedAt!!,
)

// Rebrickable set numbers: "10281-1", "fruit1-1"; the "-1" variant suffix is optional on input.
const val SET_NUMBER_PATTERN = "^[A-Za-z0-9]{1,15}(-\\d{1,3})?$"
