package fr.smo.chess.helper

import fr.smo.chess.model.*
import fr.smo.chess.service.GameStateAggregate
import fr.smo.chess.service.PseudoLegalMovesFinder
import kotlin.random.Random

class RandomAIPlayer(seed : Long = System.currentTimeMillis()) : Player {

    private var color : Color? = null
    private val pseudoLegalMovesFinder  = PseudoLegalMovesFinder()
    private val gameStateAggregate = GameStateAggregate()
    private val random = Random(seed)

    init {
        println("RandomAIPlayer seed: $seed")
    }

    override fun nextPlay(gameState: GameState): MoveRequest {
        var randomPosition : Position?
        var selectedMove : Move? = null
        var allRemainingColorPositions = gameState.chessboard.piecesOnBoard.filter { it.piece.color == color }
        while(selectedMove == null) {
            randomPosition = allRemainingColorPositions.random(random)
            allRemainingColorPositions = allRemainingColorPositions.minus(randomPosition)
            var pieceLegalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(randomPosition, gameState)
            while (pieceLegalMoves.isNotEmpty() && selectedMove == null) {
                selectedMove = pieceLegalMoves.random(random)
                pieceLegalMoves = pieceLegalMoves.minus(selectedMove)
                if (!isMoveLegal(selectedMove, gameState)) {
                    selectedMove = null
                }
            }
        }
        return MoveRequest(
            from = selectedMove.from,
            destination = selectedMove.destination,
            promotedPiece = selectedMove.promotedTo?.type,
        )
    }

    private fun isMoveLegal(move: Move, gamestate: GameState): Boolean {
        try {
            gameStateAggregate.applyMoveRequest(
                MoveRequest(
                    from = move.from,
                    destination = move.destination,
                    promotedPiece = move.promotedTo?.type,
                ), gamestate
            )
        } catch (e: Exception) {
            return false
        }
        return true
    }

    override fun registerColor(colorForTheGame: Color) {
        color = colorForTheGame
    }
}