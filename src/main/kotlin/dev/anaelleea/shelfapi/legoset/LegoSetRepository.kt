package dev.anaelleea.shelfapi.legoset

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

// The entity graphs load the theme in the same query, instead of one extra query per set.
interface LegoSetRepository : JpaRepository<LegoSet, Long> {

    @EntityGraph(attributePaths = ["theme"])
    fun findBySetNumber(setNumber: String): LegoSet?

    @EntityGraph(attributePaths = ["theme"])
    override fun findAll(pageable: Pageable): Page<LegoSet>

    @EntityGraph(attributePaths = ["theme"])
    fun findAllByStatus(status: LegoSetStatus, pageable: Pageable): Page<LegoSet>

    fun existsBySetNumber(setNumber: String): Boolean

}

interface LegoThemeRepository : JpaRepository<LegoTheme, Int>
