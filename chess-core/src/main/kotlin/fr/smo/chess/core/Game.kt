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
    val status: Status = Status.CREATED,
) {
    fun applyMove(moveCommand: MoveCommand): Game =
        buildNewGameState(moveCommand).let{ updatedGame ->
            updatedGame.checkIfCurrentColorKingIsCheckedAfterCurrentColorMove(moveCommand)
            return updatedGame.updateGameOutcome()
        }

    fun applyMoves(vararg moveCommands: MoveCommand): Game {
        return moveCommands.fold(this) { currentGame, moveRequest -> currentGame.applyMove(moveRequest) }
    }

    private fun checkIfCurrentColorKingIsCheckedAfterCurrentColorMove(move: MoveCommand) {
        if (isChecked(sideToMove.opposite())) {
            throw IllegalArgumentException(
                "Having its own king checked after performing own move is illegal. " +
                        "MoveRequest : $move, game: $this"
            )
        }
    }

    private fun buildNewGameState(moveCommand: MoveCommand): Game {
        val move: Move = buildMove(getOriginPiecePosition(moveCommand), moveCommand)
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
            .map { MoveCommand(it.origin, it.destination, it.promotedTo?.type) }
            .map { buildNewGameState(it) }
            .all { it.isChecked(colorThatMayHaveNoMove) }
    }

    private fun isCheckMate(kingColorThatMayBeCheckMate: Color): Boolean {
        if (isChecked(kingColorThatMayBeCheckMate)) {
            return chessboard.getAllPseudoLegalMovesForColor(kingColorThatMayBeCheckMate, this)
                .map { MoveCommand(it.origin, it.destination, it.promotedTo?.type) }
                .map { buildNewGameState(it) }
                .all { it.isChecked(kingColorThatMayBeCheckMate) }
        }
        return false
    }

    private fun getEnPassantTargetSquare(move: Move): Square? {
        return if (move.piece == WHITE_PAWN && move.origin.rank == RANK_2 && move.destination.rank == RANK_4) {
            move.origin.withRank(RANK_3)
        } else if (move.piece == BLACK_PAWN && move.origin.rank == RANK_7 && move.destination.rank == RANK_5) {
            move.origin.withRank(RANK_6)
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
            copy(status = Status.STARTED)
    }

    private fun buildMove(
        originPiecePosition: PiecePosition,
        moveCommand: MoveCommand
    ): Move {
        return originPiecePosition.getAllPseudoLegalMoves(this)
            .firstOrNull { it.destination == moveCommand.destination && it.promotedTo?.type == moveCommand.promotedPiece }
            ?: throw IllegalStateException("Invalid move $moveCommand")
    }

    private fun getOriginPiecePosition(moveCommand: MoveCommand): PiecePosition {
        return chessboard.getPositionAt(moveCommand.origin)
            ?: throw IllegalStateException("There is no piece at ${moveCommand.origin}")
    }
}
