package fr.smo.chess.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class ChessApplication

fun main(args: Array<String>) {
    runApplication<ChessApplication>(*args)
}
