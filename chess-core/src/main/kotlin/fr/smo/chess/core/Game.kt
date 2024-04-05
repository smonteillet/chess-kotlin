package fr.smo.chess.core

import fr.smo.chess.core.Color.WHITE
import fr.smo.chess.core.Rank.*
import fr.smo.chess.core.utils.ifTrue

data class Game(
    val chessboard: Chessboard,
    val history: History,
    val sideToMove: Color = WHITE,
    val castling: Castling,
    val enPassantTargetSquare: Square? = null,
    val status: Status = Status.CREATED,
) {
    fun applyMove(moveCommand: MoveCommand, updateGameState : Boolean = true): Game =
        buildNewGameState(moveCommand).let{ updatedGame ->
            updateGameState.ifTrue {
                updatedGame.copy(
                        status =  getOutcome(updatedGame)
                )
            }  ?: updatedGame
        }

    fun applyMoves(vararg moveCommands: MoveCommand): Game {
        return moveCommands.fold(this) { currentGame, moveRequest -> currentGame.applyMove(moveRequest) }
    }

    private fun buildNewGameState(moveCommand: MoveCommand): Game {
        val move: Move = buildMove(moveCommand)
        val newChessboard = chessboard
            .applyMoveOnBoard(move, enPassantTargetSquare)
            .applyPromotions(move)
            .applyRookMovesForCastling(move)
        return Game(
            chessboard = newChessboard,
            history = history.addMove(move),
            sideToMove = sideToMove.opposite(),
            castling = castling.updateCastlingAfterMove(move),
            enPassantTargetSquare = getEnPassantTargetSquare(move),
        ).let { updateGameState ->
            if (isChecked(updateGameState.history.lastMoveColor().opposite(), updateGameState)) {
                    updateGameState.copy(
                        history = history.addMove(move.copy(isCheck = true))
                    )
            }
            else updateGameState
        }.also {
            if (isChecked(it.sideToMove.opposite(), it)) {
                throw IllegalArgumentException(
                        "Having its own king checked after performing own move is illegal. " +
                                "MoveRequest : $moveCommand, game: $it"
                )
            }
        }
    }

    private fun getEnPassantTargetSquare(move: Move): Square? {
        return if (move.origin.rank == RANK_2 && move.destination.rank == RANK_4) {
            move.origin.withRank(RANK_3)
        } else if (move.origin.rank == RANK_7 && move.destination.rank == RANK_5) {
            move.origin.withRank(RANK_6)
        } else
            null
    }

    private fun buildMove(moveCommand: MoveCommand): Move =
        getOriginPiecePosition(moveCommand).getAllPseudoLegalMoves(this)
            .firstOrNull { it.destination == moveCommand.destination && it.promotedTo?.type == moveCommand.promotedPiece }
            ?: throw IllegalStateException("Invalid move $moveCommand")

    private fun getOriginPiecePosition(moveCommand: MoveCommand): PiecePosition {
        return chessboard.getPositionAt(moveCommand.origin)
            ?: throw IllegalStateException("There is no piece at ${moveCommand.origin}")
    }
}
