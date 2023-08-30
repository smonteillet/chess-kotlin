package fr.smo.chess.core.fixtures

import fr.smo.chess.core.Color
import fr.smo.chess.core.Color.BLACK
import fr.smo.chess.core.Color.WHITE
import fr.smo.chess.core.Game
import fr.smo.chess.core.GameFactory
import fr.smo.chess.core.MoveCommand
import fr.smo.chess.core.notation.FEN.exportFEN
import fr.smo.chess.core.notation.PGN.exportPGN

class GameLoop(
    private val afterMoveCallback: ((Game) -> Unit) = {},
) {

    fun run(player1: Player, player2: Player): Game {
        var game = GameFactory.createStandardGame()
        val players = mapOf(WHITE to player1, BLACK to player2)
        player1.registerColor(WHITE)
        player2.registerColor(BLACK)
        while (!game.status.gameIsOver) {
            val moveRequest = players[game.sideToMove]!!.nextPlay(game)
            game = game.applyMove(moveRequest)
            afterMoveCallback.invoke(game)
        }
        logGameState(game)
        return game
    }

    private fun logGameState(game: Game) {
        println(exportPGN(game))
        println(exportFEN(game))
        println(game.status)
    }

}

interface Player {
    fun nextPlay(game: Game) : MoveCommand
    fun registerColor(colorForTheGame : Color)
}