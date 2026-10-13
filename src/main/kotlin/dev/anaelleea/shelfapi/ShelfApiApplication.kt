package dev.anaelleea.shelfapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ShelfApiApplication

fun main(args: Array<String>) {
    runApplication<ShelfApiApplication>(*args)
}
