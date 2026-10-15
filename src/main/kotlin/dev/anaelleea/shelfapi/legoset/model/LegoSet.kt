package dev.anaelleea.shelfapi.legoset.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@Entity
@Table(name = "lego_sets")
class LegoSet(
    var setNumber: String,
    var name: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "theme_id")
    var theme: LegoTheme,

    var releaseYear: Short,
    var pieceCount: Int,
    var imageUrl: String?,

    @Enumerated(EnumType.STRING)
    var status: LegoSetStatus,
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @CreationTimestamp
    @Column(updatable = false)
    var addedAt: Instant? = null

    @UpdateTimestamp
    var updatedAt: Instant? = null

}
