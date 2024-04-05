package fr.smo.chess.core

import fr.smo.chess.core.Color.WHITE
import fr.smo.chess.core.Rank.*
import fr.smo.chess.core.utils.*

data class MoveCommandError(override val message: String) : OutcomeError

data class Game(
        val chessboard: Chessboard,
        val history: History,
        val sideToMove: Color = WHITE,
        val castling: Castling,
        val enPassantTargetSquare: Square? = null,
        val status: Status = Status.CREATED,
) {
    fun applyMove(moveCommand: MoveCommand, updateGameState: Boolean = true): Outcome<Game, MoveCommandError> {
        return buildMove(moveCommand).flatMap { move ->
            Game(
                    chessboard = chessboard.applyMoveOnBoard(move, enPassantTargetSquare)
                            .applyPromotions(move)
                            .applyRookMovesForCastling(move),
                    history = history.addMove(move),
                    sideToMove = sideToMove.opposite(),
                    castling = castling.updateCastlingAfterMove(move),
                    enPassantTargetSquare = getEnPassantTargetSquare(move),
            ).let { newGameState ->
                isChecked(newGameState.sideToMove.opposite(), newGameState).ifTrue {
                    Failure(MoveCommandError("Having its own king checked after performing own move is illegal. " +
                            "At move : ${newGameState.history.halfMoveClock} $moveCommand $it"))
                } ?: Success(newGameState)
            }.map { newGameState ->
                isChecked(newGameState.sideToMove, newGameState).ifTrue {
                    newGameState.copy(history = history.addMove(move.copy(isCheck = true)))
                } ?: newGameState
            }.map { updatedGame ->
                updateGameState.ifTrue {
                    updatedGame.copy(status = getOutcome(updatedGame))
                } ?: updatedGame
            }
        }
    }

    fun applyMoves(vararg moveCommands: MoveCommand): Outcome<Game, MoveCommandError> {
        return moveCommands.fold(Success(this) as Outcome<Game, MoveCommandError>) { result, moveRequest ->
            result.flatMap { it.applyMove(moveRequest) }
        }
    }

    private fun getEnPassantTargetSquare(move: Move): Square? {
        return if (move.piece == Piece.WHITE_PAWN && move.origin.rank == RANK_2 && move.destination.rank == RANK_4) {
            move.origin.withRank(RANK_3)
        } else if (move.piece == Piece.BLACK_PAWN && move.origin.rank == RANK_7 && move.destination.rank == RANK_5) {
            move.origin.withRank(RANK_6)
        } else
            null
    }

    private fun buildMove(moveCommand: MoveCommand): Outcome<Move, MoveCommandError> =
            getOriginPiecePosition(moveCommand).flatMap { piecePosition ->
                piecePosition.getAllPseudoLegalMoves(this)
                        .firstOrNull { it.destination == moveCommand.destination && it.promotedTo?.type == moveCommand.promotedPiece }
                        ?.let { Success(it) }
                        ?: Failure(MoveCommandError("There is no move at ${moveCommand.origin} to ${moveCommand.destination}"))
            }

    private fun getOriginPiecePosition(moveCommand: MoveCommand): Outcome<PiecePosition, MoveCommandError> {
        return chessboard.getPositionAt(moveCommand.origin)?.let { Success(it) }
                ?: Failure(MoveCommandError("There is no piece at ${moveCommand.origin}"))

    }
}
