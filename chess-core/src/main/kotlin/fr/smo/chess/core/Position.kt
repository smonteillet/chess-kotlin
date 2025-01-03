package fr.smo.chess.core

import fr.smo.chess.core.Color.WHITE
import fr.smo.chess.core.Rank.*
import fr.smo.chess.core.algs.getStatus
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
    fun applyMove(moveCommand: MoveCommand): Outcome<MoveCommandError, Position> {
        return moveCommand.toMove(this)
            .map { unsafeMakeMove(it) }
            .flatMap { it.isGamePositionLegal() }
            .map { it.markMoveAsCheckedInHistoryIfNecessary() }
            .map { it.updateGameStatus() }
    }

    fun isGamePositionLegal(): Outcome<MoveCommandError, Position> {
        return if (isChecked(sideToMove.opposite(), this)) {
            Failure(CannotLeaveYourOwnKingInCheck(history))
        } else {
            Success(this)
        }
    }

    private fun updateGameStatus() = copy(status = getStatus(this))

    private fun markMoveAsCheckedInHistoryIfNecessary() : Position {
        return isChecked(sideToMove, this).ifTrue {
                copy(history = history.markLastMoveAsChecked())
            } ?: this
    }

    fun unsafeMakeMove(move: Move) =
            Position(
                    chessboard = chessboard.applyMoveOnBoard(move, enPassantTargetSquare)
                            .applyPromotionIfNecessary(move)
                            .applyRookMovesAfterCastleIfNecessary(move),
                    history = history.addMove(move),
                    sideToMove = sideToMove.opposite(),
                    castling = castling.updateCastlingAfterMove(move),
                    enPassantTargetSquare = getEnPassantTargetSquare(move),
            )


    fun applyMoves(vararg moveCommands: MoveCommand): Outcome<MoveCommandError, Position> {
        return moveCommands.fold(Success(this) as Outcome<MoveCommandError, Position>) { result, moveRequest ->
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

    private fun MoveCommand.toMove(position : Position): Outcome<MoveCommandError, Move> {
        return getOriginPiecePosition(this).flatMap { piecePosition ->
            getPseudoLegalMoves(position, piecePosition)
                .firstOrNull { it.destination == this.destination && it.promotedTo?.type == this.promotedPiece }
                ?.let { Success(it) }
                ?: Failure(IllegalMove(this.origin,this.destination))
        }
    }

    private fun getOriginPiecePosition(moveCommand: MoveCommand): Outcome<MoveCommandError, PiecePosition> {
        return chessboard.getPieceAt(moveCommand.origin)?.let { Success(PiecePosition(square = moveCommand.origin, piece = it)) }
            ?: Failure(PieceNotFound(moveCommand.origin))
    }
}
