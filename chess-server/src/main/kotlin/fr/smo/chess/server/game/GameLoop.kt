package fr.smo.chess.server.game

import fr.smo.chess.core.Color.BLACK
import fr.smo.chess.core.Color.WHITE
import fr.smo.chess.core.GameState
import fr.smo.chess.core.Player
import fr.smo.chess.server.notation.FEN
import fr.smo.chess.server.notation.PGN
import fr.smo.chess.server.notation.STARTING_POSITION_FEN

class GameLoop(
    private val gameStateChangeNotifier: ((GameState) -> Unit) = {},
    private val moveApplier : MoveApplier = MoveApplier(),
    private val pgn : PGN = PGN(),
    private val fen : FEN = FEN(),
) {

    fun run(player1: Player, player2: Player): GameState {
        var gameState =  fen.import(STARTING_POSITION_FEN)
        val players = mapOf(WHITE to player1, BLACK to player2)
        player1.registerColor(WHITE)
        player2.registerColor(BLACK)
        while (gameState.gameOutcome == GameState.GameOutcome.NOT_FINISHED_YET) {
            val moveRequest = players[gameState.sideToMove]!!.nextPlay(gameState)
            gameState = moveApplier.applyMoveRequest(moveRequest, gameState)
            gameStateChangeNotifier.invoke(gameState)
        }
        logGameState(gameState)
        return gameState
    }

    private fun logGameState(gameState: GameState) {
        println(pgn.export(gameState))
        println(fen.export(gameState))
        println(gameState.gameOutcome)
    }

}