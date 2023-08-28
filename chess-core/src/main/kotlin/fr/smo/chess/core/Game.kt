package fr.smo.chess.core

import fr.smo.chess.core.Color.WHITE
import fr.smo.chess.core.Piece.BLACK_PAWN
import fr.smo.chess.core.Piece.WHITE_PAWN
import fr.smo.chess.core.PieceType.KING
import fr.smo.chess.core.Rank.*

data class Game(
    val chessboard: Chessboard,
    val history: History,
    val sideToMove: Color = WHITE,
    val castling: Castling,
    val enPassantTargetSquare: Square? = null,
    val status: Status = Status.NOT_STARTED_YET,
) {
    val gameIsOver: Boolean = when (status) {
        Status.BLACK_WIN, Status.WHITE_WIN, Status.DRAW  -> true
        else -> false
    }

    enum class Status {
        NOT_STARTED_YET,
        IN_PROGRESS,
        BLACK_WIN,
        WHITE_WIN,
        DRAW,
    }

    fun applyMove(moveRequest: MoveRequest, afterMoveCallback: (Game) -> Unit = {}): Game =
        buildNewGameState(moveRequest).let{ updatedGame ->
            updatedGame.checkIfCurrentColorKingIsCheckedAfterCurrentColorMove(moveRequest)
            return updatedGame.updateGameOutcome().also { afterMoveCallback.invoke(it) }
        }

    fun applyMoves(vararg moveRequests: MoveRequest): Game {
        return moveRequests.fold(this) { currentGame, moveRequest -> currentGame.applyMove(moveRequest) }
    }

    private fun checkIfCurrentColorKingIsCheckedAfterCurrentColorMove(move: MoveRequest) {
        if (isChecked(sideToMove.opposite())) {
            throw IllegalArgumentException(
                "Having its own king checked after performing own move is illegal. " +
                        "MoveRequest : $move, game: $this"
            )
        }
    }

    private fun buildNewGameState(moveRequest: MoveRequest): Game {
        val move: Move = buildMove(getFromPosition(moveRequest), moveRequest)
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
        )
    }

    private fun isChecked(kingColorThatMayBeChecked: Color): Boolean {
        return chessboard.getAllPseudoLegalMovesForColor(kingColorThatMayBeChecked.opposite(), this)
            .count { it.capturedPiece?.type == KING } > 0
    }

    private fun isStaleMate(colorThatMayHaveNoMove: Color): Boolean {
        return chessboard.getAllPseudoLegalMovesForColor(colorThatMayHaveNoMove, this)
            .map { MoveRequest(it.from, it.destination, it.promotedTo?.type) }
            .map { buildNewGameState(it) }
            .all { it.isChecked(colorThatMayHaveNoMove) }
    }

    private fun isCheckMate(kingColorThatMayBeCheckMate: Color): Boolean {
        if (isChecked(kingColorThatMayBeCheckMate)) {
            return chessboard.getAllPseudoLegalMovesForColor(kingColorThatMayBeCheckMate, this)
                .map { MoveRequest(it.from, it.destination, it.promotedTo?.type) }
                .map { buildNewGameState(it) }
                .all { it.isChecked(kingColorThatMayBeCheckMate) }
        }
        return false
    }

    private fun getEnPassantTargetSquare(move: Move): Square? {
        return if (move.piece == WHITE_PAWN && move.from.rank == RANK_2 && move.destination.rank == RANK_4) {
            move.from.withRank(RANK_3)
        } else if (move.piece == BLACK_PAWN && move.from.rank == RANK_7 && move.destination.rank == RANK_5) {
            move.from.withRank(RANK_6)
        } else
            null
    }

    private fun updateGameOutcome(): Game {
        return if (isCheckMate(sideToMove)) {
            val status = if (sideToMove == WHITE) {
                Status.BLACK_WIN
            } else {
                Status.WHITE_WIN
            }
            copy(status = status)
        } else if (history.isFiftyMovesRule()) {
            copy(status = Status.DRAW)
        } else if (history.isThreefoldRepetitions()) {
            copy(status = Status.DRAW)
        } else if (isStaleMate(sideToMove)) {
            copy(status = Status.DRAW)
        } else
            copy(status = Status.IN_PROGRESS)
    }

    private fun buildMove(
        fromPosition: Position,
        moveRequest: MoveRequest
    ): Move {
        return fromPosition.getAllPseudoLegalMoves(this)
            .firstOrNull { it.destination == moveRequest.destination && it.promotedTo?.type == moveRequest.promotedPiece }
            ?: throw IllegalStateException("Invalid move $moveRequest")
    }

    private fun getFromPosition(moveRequest: MoveRequest): Position {
        return chessboard.getPositionAt(moveRequest.from)
            ?: throw IllegalStateException("There is no piece at ${moveRequest.from}")
    }
}
