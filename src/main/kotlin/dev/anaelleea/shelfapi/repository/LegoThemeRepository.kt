package dev.anaelleea.shelfapi.repository

import dev.anaelleea.shelfapi.entity.LegoTheme
import org.springframework.data.jpa.repository.JpaRepository

interface LegoThemeRepository : JpaRepository<LegoTheme, Int>
