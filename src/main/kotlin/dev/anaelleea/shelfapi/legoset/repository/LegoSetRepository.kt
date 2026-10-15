package dev.anaelleea.shelfapi.legoset.repository

import dev.anaelleea.shelfapi.legoset.model.LegoSet
import dev.anaelleea.shelfapi.legoset.model.LegoSetStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.Instant

// The entity graphs load the theme in the same query, instead of one extra query per set.
interface LegoSetRepository : JpaRepository<LegoSet, Long> {

    @EntityGraph(attributePaths = ["theme"])
    fun findBySetNumber(setNumber: String): LegoSet?

    @EntityGraph(attributePaths = ["theme"])
    override fun findAll(pageable: Pageable): Page<LegoSet>

    @EntityGraph(attributePaths = ["theme"])
    fun findAllByStatus(status: LegoSetStatus, pageable: Pageable): Page<LegoSet>

    /** Changes the status only if it is still [from]; returns the number of updated rows (0 or 1). */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update LegoSet s set s.status = :to, s.updatedAt = :now where s.setNumber = :setNumber and s.status = :from")
    fun updateStatusIf(setNumber: String, from: LegoSetStatus, to: LegoSetStatus, now: Instant): Int

    fun existsBySetNumber(setNumber: String): Boolean

    @Query("select s.setNumber from LegoSet s where s.setNumber in :setNumbers")
    fun findSetNumbersIn(setNumbers: Collection<String>): List<String>

}
