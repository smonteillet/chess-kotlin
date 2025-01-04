package fr.smo.chess.core

import fr.smo.chess.core.Color.WHITE
import fr.smo.chess.core.Rank.*
import fr.smo.chess.core.algs.getPseudoLegalMoves
import fr.smo.chess.core.algs.hasAPseudoLegalMovesSatisfying
import fr.smo.chess.core.algs.isChecked
import fr.smo.chess.core.utils.*
import fr.smo.chess.core.variant.Standard
import fr.smo.chess.core.variant.Variant

data class Position(
    val chessboard: Chessboard,
    val history: History = History(),
    val sideToMove: Color = WHITE,
    val enPassantTargetSquare: Square? = null,
    val forcedOutcome: ForcedOutcome? = null,
    val variant : Variant = Standard,
    val castles: Castles = variant.initCastles(chessboard),
) {

    private val isCheck: Boolean
        get() = history.lastMove?.isCheck ?: false

    val status: Status
        get() = when (forcedOutcome) {
            ForcedOutcome.BLACK_RESIGN -> Status.WHITE_WIN
            ForcedOutcome.WHITE_RESIGN -> Status.BLACK_WIN
            ForcedOutcome.DRAW_AGREEMENT -> Status.DRAW
            ForcedOutcome.UNKNOWN_RESULT -> Status.UNKNOWN_RESULT
            null -> if (variant.isVariantVictory(this)) {
                if (history.lastMovedColor == WHITE) {
                    Status.WHITE_WIN
                } else {
                    Status.BLACK_WIN
                }
            } else if (variant.isVariantDraw(this)) {
                Status.DRAW
            } else {
                Status.STARTED
            }
        }

    fun applyMove(moveCommand: MoveCommand): Outcome<MoveCommandError, Position> {
        return moveCommand.toMove(this)
            .map { unsafeMakeMove(it) }
            .flatMap { it.isGamePositionLegal() }
            .map { it.markMoveAsCheckedInHistoryIfNecessary() }
    }

    fun applyMoves(vararg moveCommands: MoveCommand): Outcome<MoveCommandError, Position> {
        return moveCommands.fold(Success(this) as Outcome<MoveCommandError, Position>) { result, moveRequest ->
            result.flatMap { it.applyMove(moveRequest) }
        }
    }

    fun forceOutcome(forcedOutcome: ForcedOutcome): Position {
        return copy(forcedOutcome = forcedOutcome)
    }

    private fun isGamePositionLegal(): Outcome<MoveCommandError, Position> {
        return if (isChecked(sideToMove.opposite(), this)) {
            Failure(CannotLeaveYourOwnKingInCheck(history))
        } else {
            Success(this)
        }
    }

    private fun hasNoLegalMove(): Boolean {
        return hasAPseudoLegalMovesSatisfying(sideToMove, this) { move ->
            when (this.unsafeMakeMove(move).isGamePositionLegal()) {
                is Success -> true
                is Failure -> false
            }
        }.not()
    }


    private fun markMoveAsCheckedInHistoryIfNecessary(): Position {
        return isChecked(sideToMove, this).ifTrue {
            copy(history = history.markLastMoveAsChecked())
        } ?: this
    }

    private fun unsafeMakeMove(move: Move) =
        Position(
            chessboard = chessboard.applyMove(move, enPassantTargetSquare, castles),
            history = history.addMove(move),
            sideToMove = sideToMove.opposite(),
            castles = castles.updateCastlesAfterMove(move),
            enPassantTargetSquare = getEnPassantTargetSquare(move),
            variant = variant
        )

    private fun getEnPassantTargetSquare(move: Move): Square? {
        return if (move.piece == Piece.WHITE_PAWN && move.origin.rank == RANK_2 && move.destination.rank == RANK_4) {
            move.origin.withRank(RANK_3)
        } else if (move.piece == Piece.BLACK_PAWN && move.origin.rank == RANK_7 && move.destination.rank == RANK_5) {
            move.origin.withRank(RANK_6)
        } else
            null
    }

    private fun MoveCommand.toMove(position: Position): Outcome<MoveCommandError, Move> {
        return getOriginPiecePosition(this).flatMap { piecePosition ->
            getPseudoLegalMoves(position, piecePosition)
                .firstOrNull { it.destination == this.destination && it.promotedTo?.type == this.promotedPiece }
                ?.let { Success(it) }
                ?: Failure(IllegalMove(this.origin, this.destination))
        }
    }

    private fun getOriginPiecePosition(moveCommand: MoveCommand): Outcome<MoveCommandError, PiecePosition> {
        return chessboard.getPieceAt(moveCommand.origin)
            ?.let { Success(PiecePosition(square = moveCommand.origin, piece = it)) }
            ?: Failure(PieceNotFound(moveCommand.origin))
    }

    fun isCheckMate() = isCheck && hasNoLegalMove()

    private fun isStaleMate() = !isCheckMate() && hasNoLegalMove()

    // FIXME need to implement insufficient material rule
    // https://www.chess.com/article/view/how-chess-games-can-end-8-ways-explained#insufficient-material
    fun isDraw() = history.isFiftyMoveRules || history.isThreeFoldRepetition || isStaleMate()


    enum class ForcedOutcome {
        BLACK_RESIGN,
        WHITE_RESIGN,
        DRAW_AGREEMENT,
        UNKNOWN_RESULT,
    }
}
