package fr.smo.chess.core.fixtures

import fr.smo.chess.core.*
import fr.smo.chess.core.utils.Failure
import fr.smo.chess.core.utils.Success
import kotlin.random.Random

class RandomAIPlayer(seed : Long = System.currentTimeMillis()) : Player {

    private var color : Color? = null
    private val random = Random(seed)

    init {
        println("RandomAIPlayer seed: $seed")
    }

    override fun nextPlay(game: Game): MoveCommand {
        var randomPiecePosition : PiecePosition?
        var selectedMove : Move? = null
        var allRemainingColorPositions = game.chessboard.piecesOnBoard.filter { it.piece.color == color }
        while(selectedMove == null) {
            randomPiecePosition = allRemainingColorPositions.random(random)
            allRemainingColorPositions = allRemainingColorPositions.minus(randomPiecePosition)
            var pieceLegalMoves = randomPiecePosition.getAllPseudoLegalMoves(game)
            while (pieceLegalMoves.isNotEmpty() && selectedMove == null) {
                selectedMove = pieceLegalMoves.random(random)
                pieceLegalMoves = pieceLegalMoves.minus(selectedMove)
                if (!isMoveLegal(selectedMove, game)) {
                    selectedMove = null
                }
            }
        }
        return MoveCommand(
            origin = selectedMove.origin,
            destination = selectedMove.destination,
            promotedPiece = selectedMove.promotedTo?.type,
        )
    }

    private fun isMoveLegal(move: Move, game: Game): Boolean {
        return game.applyMove(
            MoveCommand(
                origin = move.origin,
                destination = move.destination,
                promotedPiece = move.promotedTo?.type,
            )
        ).let { outcome ->
            when (outcome) {
                is Success -> true
                is Failure -> false
            }
        }
    }

    override fun registerColor(colorForTheGame: Color) {
        color = colorForTheGame
    }
}