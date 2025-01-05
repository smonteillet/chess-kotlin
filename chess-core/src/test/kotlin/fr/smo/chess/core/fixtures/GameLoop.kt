package fr.smo.chess.core.fixtures

import fr.smo.chess.core.Color
import fr.smo.chess.core.Color.BLACK
import fr.smo.chess.core.Color.WHITE
import fr.smo.chess.core.GameFactory
import fr.smo.chess.core.MoveCommand
import fr.smo.chess.core.Position
import fr.smo.chess.core.notation.FEN.exportFEN
import fr.smo.chess.core.notation.PGN.exportPGN
import fr.smo.chess.core.variant.Chess960
import fr.smo.chess.core.variant.Standard
import fr.smo.chess.core.variant.Variant
import kotlin.random.Random

class GameLoop(
    private val afterMoveCallback: (Position) -> Unit = {},
    val seed: Long,
    private val random: Random = Random(seed),
) {


    fun run(player1: Player, player2: Player): Position {
        var game = getStartingPosition()
        val players = mapOf(WHITE to player1, BLACK to player2)
        player1.registerColor(WHITE)
        player2.registerColor(BLACK)
        while (!game.status.gameIsOver) {
            val moveRequest = players[game.sideToMove]!!.nextPlay(game)
            game = game.applyMove(moveRequest).orThrow()
            afterMoveCallback.invoke(game)
        }
        logGameState(game)
        return game
    }

    private fun logGameState(position: Position) {
        println(exportPGN(position))
        println(exportFEN(position))
        println(position.status)
    }


    private fun getStartingPosition(): Position {
        val randNumber = random.nextFloat()
        val variant = if (randNumber <= 0.5f) {
            println("Chosen variant : Standard")
            Standard
        } else {
            Variant::class.sealedSubclasses
                .filterNot { it.objectInstance is Standard }
                .random(random)
                .objectInstance
        }
        println("Chosen variant : $variant")
        return if (variant is Chess960) {
            GameFactory.create960Game(random = random)
        } else GameFactory.createGame(variant!!)
    }
}

interface Player {
    fun nextPlay(position: Position): MoveCommand
    fun registerColor(colorForTheGame: Color)
}