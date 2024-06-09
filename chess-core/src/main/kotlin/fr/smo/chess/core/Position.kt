package fr.smo.chess.core

import fr.smo.chess.core.Color.WHITE
import fr.smo.chess.core.Rank.*
import fr.smo.chess.core.algs.getOutcome
import fr.smo.chess.core.algs.getPseudoLegalMoves
import fr.smo.chess.core.algs.isChecked
import fr.smo.chess.core.utils.*

data class Position(
        val chessboard: Chessboard,
        val history: History,
        val sideToMove: Color = WHITE,
        val castling: Castling,
        val enPassantTargetSquare: Square? = null,
        val status: Status = Status.CREATED,
) {
    fun applyMove(moveCommand: MoveCommand): Outcome<Position, MoveCommandError> {
        return isLegalMove(moveCommand)
                    .map { markMoveAsCheckedInHistory(it) }
                    .map { updatedGame -> updateGameOutcome(updatedGame) }
    }

    fun isLegalMove(moveCommand: MoveCommand): Outcome<Position, MoveCommandError> {
        return buildMove(moveCommand).flatMap { move ->
            isGamePositionLegal(computeNewPositionAfterMakeMove(move))
        }
    }

    private fun isGamePositionLegal(position: Position): Outcome<Position, MoveCommandError> {
        return if (isChecked(position.sideToMove.opposite(), position)) {
            Failure(CannotLeaveYourOwnKingInCheck(position.history))
        } else {
            Success(position)
        }
    }

    private fun updateGameOutcome(position: Position) = position.copy(status = getOutcome(position))

    private fun markMoveAsCheckedInHistory(position: Position) : Position {
        return isChecked(position.sideToMove, position).ifTrue {
                position.copy(history = history.addMove(position.history.lastMove().copy(isCheck = true)))
            } ?: position
    }

    private fun computeNewPositionAfterMakeMove(move: Move) =
            Position(
                    chessboard = chessboard.applyMoveOnBoard(move, enPassantTargetSquare)
                            .applyPromotionIfNecessary(move)
                            .applyRookMovesAfterCastleIfNecessary(move),
                    history = history.addMove(move),
                    sideToMove = sideToMove.opposite(),
                    castling = castling.updateCastlingAfterMove(move),
                    enPassantTargetSquare = getEnPassantTargetSquare(move),
            )


    fun applyMoves(vararg moveCommands: MoveCommand): Outcome<Position, MoveCommandError> {
        return moveCommands.fold(Success(this) as Outcome<Position, MoveCommandError>) { result, moveRequest ->
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
                getPseudoLegalMoves(this, piecePosition)
                        .firstOrNull { it.destination == moveCommand.destination && it.promotedTo?.type == moveCommand.promotedPiece }
                        ?.let { Success(it) }
                        ?: Failure(IllegalMove(moveCommand.origin,moveCommand.destination))
            }

    private fun getOriginPiecePosition(moveCommand: MoveCommand): Outcome<PiecePosition, MoveCommandError> {
        return chessboard.getPieceAt(moveCommand.origin)?.let { Success(PiecePosition(square = moveCommand.origin, piece = it)) }
                ?: Failure(PieceNotFound(moveCommand.origin))
    }
}
