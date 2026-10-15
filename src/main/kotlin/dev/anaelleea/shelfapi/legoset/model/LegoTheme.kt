package dev.anaelleea.shelfapi.legoset.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

/** A Rebrickable theme, cached locally so each one is only fetched once. */
@Entity
@Table(name = "lego_themes")
class LegoTheme(
    @Id
    val id: Int,
    var name: String,
)
