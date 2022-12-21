package fr.smo.chess.core.fixtures

import fr.smo.chess.core.*
import kotlin.random.Random

class RandomAIPlayer(seed : Long = System.currentTimeMillis()) : Player {

    private var color : Color? = null
    private val random = Random(seed)

    init {
        println("RandomAIPlayer seed: $seed")
    }

    override fun nextPlay(game: Game): MoveRequest {
        var randomPosition : Position?
        var selectedMove : Move? = null
        var allRemainingColorPositions = game.chessboard.piecesOnBoard.filter { it.piece.color == color }
        while(selectedMove == null) {
            randomPosition = allRemainingColorPositions.random(random)
            allRemainingColorPositions = allRemainingColorPositions.minus(randomPosition)
            var pieceLegalMoves = randomPosition.getAllPseudoLegalMoves(game)
            while (pieceLegalMoves.isNotEmpty() && selectedMove == null) {
                selectedMove = pieceLegalMoves.random(random)
                pieceLegalMoves = pieceLegalMoves.minus(selectedMove)
                if (!isMoveLegal(selectedMove, game)) {
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

    private fun isMoveLegal(move: Move, game: Game): Boolean {
        try {
            game.applyMove(
                MoveRequest(
                    from = move.from,
                    destination = move.destination,
                    promotedPiece = move.promotedTo?.type,
                )
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