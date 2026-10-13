package dev.anaelleea.shelfapi

import org.springframework.boot.fromApplication
import org.springframework.boot.with

// Runs the app against a throwaway Testcontainers database, without compose.yaml.
fun main(args: Array<String>) {
    fromApplication<ShelfApiApplication>().with(TestcontainersConfiguration::class).run(*args)
}
