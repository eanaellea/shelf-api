package dev.anaelleea.shelfapi.legoset.repository

import dev.anaelleea.shelfapi.legoset.model.LegoTheme
import org.springframework.data.jpa.repository.JpaRepository

interface LegoThemeRepository : JpaRepository<LegoTheme, Int>
