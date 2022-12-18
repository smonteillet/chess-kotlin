package fr.smo.chess.server.ai

import fr.smo.chess.core.*
import fr.smo.chess.server.game.MoveApplier
import fr.smo.chess.server.game.PseudoLegalMovesFinder


class NegamaxAIPlayer(private val depth: Int) : Player {

    private lateinit var color: Color
    private var iterationCount = 0
    private val pseudoLegalMoveFinder = PseudoLegalMovesFinder()
    private val moveApplier = MoveApplier(pseudoLegalMoveFinder)

    override fun nextPlay(gameState: GameState): MoveRequest {
        iterationCount = 0
        return negamax(depth, gameState).second!!
    }

    override fun registerColor(colorForTheGame: Color) {
        color = colorForTheGame
    }

    private fun negamax(depth: Int, gameState: GameState): Pair<Long, MoveRequest?> {

        iterationCount += 1
        println(iterationCount)

        if (depth == 0) {
            return evaluate(gameState) to null
        }
        var max = Long.MIN_VALUE
        var maxMove: MoveRequest? = null
        getAllMoves(gameState).forEach {
            val moveRequest = toMoveRequest(it)
            val appliedGameState = moveApplier.applyMoveRequest(moveRequest, gameState)
            val (score, _) = negamax(depth - 1, appliedGameState)
            if (score * -1 > max) {
                max = score * -1
                maxMove = moveRequest
            }
        }
        return max to maxMove
    }

    private fun evaluate(gameState: GameState): Long {
        val materialValue = gameState.chessboard.piecesOnBoard
            .filter { it.piece.color == this.color }
            .fold(0L) { score, position ->
                return score + when (position.piece.type) {
                    PieceType.PAWN -> 1
                    PieceType.BISHOP -> 3
                    PieceType.KNIGHT -> 3
                    PieceType.QUEEN -> 9
                    PieceType.KING -> 100
                    PieceType.ROOK -> 5
                }
            }
        val amountOfBlackPieces = gameState.chessboard.piecesOnBoard.count { it.piece.color == Color.BLACK }
        val amountOfWhitePieces = gameState.chessboard.piecesOnBoard.count { it.piece.color == Color.WHITE }
        val whoToMove = if (gameState.sideToMove == Color.WHITE) 1 else -1
        return materialValue * (amountOfWhitePieces - amountOfBlackPieces) * whoToMove
    }

    private fun getAllMoves(gameState: GameState): List<Move> {
        return pseudoLegalMoveFinder.getAllPseudoLegalMovesForPlayer(gameState.sideToMove, gameState).map {
            try {
                moveApplier.applyMoveRequest(toMoveRequest(it), gameState)
            } catch (e: Exception) {
                return@map null
            }
            return@map it
        }.filterNotNull()
    }

    private fun toMoveRequest(move: Move): MoveRequest {
        return MoveRequest(
            from = move.from,
            destination = move.destination,
            promotedPiece = move.promotedTo?.type,
        )
    }
/**
 * int negaMax( int depth ) {
if ( depth == 0 ) return evaluate();
int max = -oo;
for ( all moves)  {
score = -negaMax( depth - 1 );
if( score > max )
max = score;
}
return max;
}
 */

}
