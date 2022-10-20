package fr.smo.chess.service

import fr.smo.chess.model.Color.BLACK
import fr.smo.chess.model.Color.WHITE
import fr.smo.chess.model.GameState
import fr.smo.chess.model.Player

class GameLoop(
    private val gameStateChangeNotifier: ((GameState) -> Unit) = {},
    private val gameStateAggregate : GameStateAggregate = GameStateAggregate(),
    private val pgn : PGN = PGN(),
    private val fen : FEN = FEN(),
) {

    fun run(player1: Player, player2: Player): GameState {
        var gameState = GameState()
        val players = mapOf(WHITE to player1, BLACK to player2)
        player1.registerColor(WHITE)
        player2.registerColor(BLACK)
        while (gameState.gameOutcome == GameState.GameOutcome.NOT_FINISHED_YET) {
            val moveRequest = players[gameState.sideToMove]!!.nextPlay(gameState)
            gameState = gameStateAggregate.applyMoveRequest(moveRequest, gameState)
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