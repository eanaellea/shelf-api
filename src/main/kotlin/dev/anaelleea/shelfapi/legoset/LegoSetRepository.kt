package dev.anaelleea.shelfapi.legoset

import dev.anaelleea.shelfapi.user.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.Instant

// Every query takes the user, so each user only ever sees and changes the sets on their own shelf.
// The entity graphs load the theme in the same query, instead of one extra query per set.
interface LegoSetRepository : JpaRepository<LegoSet, Long> {

    @EntityGraph(attributePaths = ["theme"])
    fun findByUserAndSetNumber(user: User, setNumber: String): LegoSet?

    @EntityGraph(attributePaths = ["theme"])
    fun findAllByUser(user: User, pageable: Pageable): Page<LegoSet>

    @EntityGraph(attributePaths = ["theme"])
    fun findAllByUserAndStatus(user: User, status: LegoSetStatus, pageable: Pageable): Page<LegoSet>

    /** Changes the status only if it is still [from]; returns the number of updated rows (0 or 1). */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        "update LegoSet s set s.status = :to, s.updatedAt = :now " +
            "where s.user = :user and s.setNumber = :setNumber and s.status = :from"
    )
    fun updateStatusIf(user: User, setNumber: String, from: LegoSetStatus, to: LegoSetStatus, now: Instant): Int

    fun existsByUserAndSetNumber(user: User, setNumber: String): Boolean

    @Query("select s.setNumber from LegoSet s where s.user = :user and s.setNumber in :setNumbers")
    fun findSetNumbersIn(user: User, setNumbers: Collection<String>): List<String>

}

interface LegoThemeRepository : JpaRepository<LegoTheme, Int>
