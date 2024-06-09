package fr.smo.chess.core.fixtures

import fr.smo.chess.core.Color
import fr.smo.chess.core.Color.BLACK
import fr.smo.chess.core.Color.WHITE
import fr.smo.chess.core.Position
import fr.smo.chess.core.GameFactory
import fr.smo.chess.core.MoveCommand
import fr.smo.chess.core.notation.FEN.exportFEN
import fr.smo.chess.core.notation.PGN.exportPGN

class GameLoop(
    private val afterMoveCallback: ((Position) -> Unit) = {},
) {

    fun run(player1: Player, player2: Player): Position {
        var game = GameFactory.createStandardGame()
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

}

interface Player {
    fun nextPlay(position: Position) : MoveCommand
    fun registerColor(colorForTheGame : Color)
}