package dev.anaelleea.shelfapi.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

/** An invited user, who owns one shelf. */
@Entity
@Table(name = "users")
class User(
    /** The Firebase Authentication UID: the subject of the user's ID tokens. */
    val firebaseUid: String,

    /** Chosen by the admin; used in public shelf URLs, e.g. /api/shelves/{handle}/lego-sets. */
    val handle: String,
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @CreationTimestamp
    @Column(updatable = false)
    var createdAt: Instant? = null

}
