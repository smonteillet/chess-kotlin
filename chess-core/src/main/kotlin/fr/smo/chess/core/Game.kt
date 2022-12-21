package fr.smo.chess.core

import fr.smo.chess.core.Color.BLACK
import fr.smo.chess.core.Color.WHITE
import fr.smo.chess.core.Piece.*
import fr.smo.chess.core.PieceType.KING
import fr.smo.chess.core.PieceType.PAWN
import fr.smo.chess.core.PseudoLegalMovesFinder.getAllPseudoLegalMoves
import fr.smo.chess.core.PseudoLegalMovesFinder.getAllPseudoLegalMovesForPlayer
import fr.smo.chess.core.Rank.*
import fr.smo.chess.core.Square.*

data class Game(
    val chessboard: Chessboard,
    val moveHistory: List<Move> = listOf(),  // FIXME put that property in a History class
    val sideToMove: Color = WHITE,
    val castling: Castling,
    val enPassantTargetSquare: Square? = null,
    val status: Status = Status.NOT_STARTED_YET,
    val fullMoveCounter: Int = 1, // FIXME put that property in a History class
    val halfMoveClock: Int = 0, // FIXME put that property in a History class
) {
    val isNextPlayCouldBeWhiteCastle: Boolean = sideToMove == WHITE &&
            castling.isWhiteKingCastlePossible &&
            castling.isWhiteQueenCastlePossible

    val isNextPlayCouldBeBlackCastle: Boolean = sideToMove == BLACK &&
            castling.isBlackKingCastlePossible &&
            castling.isBlackQueenCastlePossible

    val gameIsOver: Boolean = status == Status.BLACK_WIN || status == Status.WHITE_WIN || status == Status.DRAW

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
        val fromPosition = getFromPosition(moveRequest)
        val move: Move = extractContextualizedMove(fromPosition, moveRequest)
        //FIXME code is not clear here
        val newBoardPositions = applyRookMovesForCastling(
            move,
            applyPromotions(
                move,
                applyMoveOnBoard(move)
            )
        )
        return Game(
            chessboard = Chessboard(piecesOnBoard = newBoardPositions),
            moveHistory = moveHistory.plus(move),
            sideToMove = sideToMove.opposite(),
            castling = Castling(
                isBlackKingCastlePossible = isBlackKingSideCastlingStillPossible(move),
                isBlackQueenCastlePossible = isBlackQueenSideCastlingStillPossible(move),
                isWhiteQueenCastlePossible = isWhiteQueenSideCastlingStillPossible(move),
                isWhiteKingCastlePossible = isWhiteKingSideCastlingStillPossible(move),
            ),
            enPassantTargetSquare = getEnPassantTargetSquare(move),
            fullMoveCounter = getNewFullMoveCounter(move),
            halfMoveClock = getHalfMoveClock(move),
        )
    }

    private fun isChecked(kingColorThatMayBeChecked: Color): Boolean {
        return getAllPseudoLegalMovesForPlayer(kingColorThatMayBeChecked.opposite(), this)
            .count { it.capturedPiece?.type == KING } > 0
    }

    private fun isStaleMate(colorThatMayHaveNoMove: Color): Boolean {
        return getAllPseudoLegalMovesForPlayer(colorThatMayHaveNoMove, this)
            .map { MoveRequest(it.from, it.destination, it.promotedTo?.type) }
            .map { buildNewGameState(it) }
            .all { it.isChecked(colorThatMayHaveNoMove) }
    }

    private fun isCheckMate(kingColorThatMayBeCheckMate: Color): Boolean {
        if (isChecked(kingColorThatMayBeCheckMate)) {
            return getAllPseudoLegalMovesForPlayer(kingColorThatMayBeCheckMate, this)
                .map { MoveRequest(it.from, it.destination, it.promotedTo?.type) }
                .map { buildNewGameState(it) }
                .all { it.isChecked(kingColorThatMayBeCheckMate) }
        }
        return false
    }

    private fun getHalfMoveClock(move: Move): Int {
        return if (move.capturedPiece != null || move.piece.type == PAWN) {
            0
        } else {
            halfMoveClock + 1
        }
    }

    private fun getNewFullMoveCounter(move: Move): Int {
        return if (move.piece.color == BLACK) {
            fullMoveCounter + 1
        } else {
            fullMoveCounter
        }
    }

    private fun getEnPassantTargetSquare(move: Move): Square? {
        return if (move.piece == WHITE_PAWN && move.from.rank == RANK_2 && move.destination.rank == RANK_4) {
            move.from.withRank(RANK_3)
        } else if (move.piece == BLACK_PAWN && move.from.rank == RANK_7 && move.destination.rank == RANK_5) {
            move.from.withRank(RANK_6)
        } else
            null
    }

    private fun is3Repetitions(): Boolean {
        val moveCount = moveHistory.size
        return moveHistory.size >= 12 &&
                moveHistory[moveCount - 1] == moveHistory[moveCount - 5] &&
                moveHistory[moveCount - 1] == moveHistory[moveCount - 9] &&
                moveHistory[moveCount - 2] == moveHistory[moveCount - 6] &&
                moveHistory[moveCount - 2] == moveHistory[moveCount - 10] &&
                moveHistory[moveCount - 3] == moveHistory[moveCount - 7] &&
                moveHistory[moveCount - 3] == moveHistory[moveCount - 11] &&
                moveHistory[moveCount - 4] == moveHistory[moveCount - 8] &&
                moveHistory[moveCount - 4] == moveHistory[moveCount - 12]

    }

    private fun updateGameOutcome(): Game {
        return if (isCheckMate(sideToMove)) {
            val status = if (sideToMove == WHITE) {
                Status.BLACK_WIN
            } else {
                Status.WHITE_WIN
            }
            copy(status = status)
        } else if (halfMoveClock == 50) {
            copy(status = Status.DRAW)
        } else if (is3Repetitions()) {
            copy(status = Status.DRAW)
        } else if (isStaleMate(sideToMove)) {
            copy(status = Status.DRAW)
        } else
            copy(status = Status.IN_PROGRESS)
    }

    private fun extractContextualizedMove(
        fromPosition: Position,
        moveRequest: MoveRequest
    ): Move {
        return getAllPseudoLegalMoves(fromPosition, this)
            .firstOrNull { it.destination == moveRequest.destination && it.promotedTo?.type == moveRequest.promotedPiece }
            ?: throw IllegalStateException("Invalid move $moveRequest")
    }

    private fun getFromPosition(moveRequest: MoveRequest): Position {
        return chessboard.getPositionAt(moveRequest.from)
            ?: throw IllegalStateException("There is no piece at ${moveRequest.from}")
    }

    private fun isWhiteKingSideCastlingStillPossible(move: Move): Boolean {
        return castling.isWhiteKingCastlePossible &&
                (
                        (
                                move.piece.color == BLACK &&
                                        ((move.capturedPiece != null && move.destination != H1) || move.capturedPiece == null)
                                ) ||
                                (
                                        move.piece.color == WHITE &&
                                                move.piece != WHITE_KING &&
                                                (move.piece != WHITE_ROOK || move.from != H1)
                                        )
                        )
    }

    private fun isWhiteQueenSideCastlingStillPossible(move: Move): Boolean {
        return castling.isWhiteQueenCastlePossible &&
                (
                        (
                                move.piece.color == BLACK &&
                                        ((move.capturedPiece != null && move.destination != A1) || move.capturedPiece == null)
                                ) ||
                                (
                                        move.piece.color == WHITE &&
                                                move.piece != WHITE_KING &&
                                                (move.piece != WHITE_ROOK || move.from != A1)
                                        )
                        )
    }

    private fun isBlackKingSideCastlingStillPossible(move: Move): Boolean {
        return castling.isBlackKingCastlePossible &&
                (
                        (
                                move.piece.color == WHITE &&
                                        ((move.capturedPiece != null && move.destination != H8) || move.capturedPiece == null)
                                ) ||
                                (
                                        move.piece.color == BLACK &&
                                                move.piece != BLACK_KING &&
                                                (move.piece != BLACK_ROOK || move.from != H8)
                                        )
                        )
    }

    private fun isBlackQueenSideCastlingStillPossible(move: Move): Boolean {
        return castling.isBlackQueenCastlePossible &&
                (
                        (
                                move.piece.color == WHITE &&
                                        ((move.capturedPiece != null && move.destination != A8) || move.capturedPiece == null)
                                ) ||
                                (
                                        move.piece.color == BLACK &&
                                                move.piece != BLACK_KING &&
                                                (move.piece != BLACK_ROOK || move.from != A8)
                                        )
                        )
    }

    private fun applyMoveOnBoard(move: Move): List<Position> {
        val opponentPawnThatHasBeenEnPassant =
            if (move.destination == enPassantTargetSquare && move.piece.type == PAWN) {
                if (move.piece.color == WHITE) {
                    enPassantTargetSquare.down()!!
                } else {
                    enPassantTargetSquare.up()!!
                }
            } else {
                null
            }
        return chessboard.piecesOnBoard
            .filter { it.square != move.from }
            .filter { it.square != move.destination }
            .filter { it.square != opponentPawnThatHasBeenEnPassant }
            .plus(Position(move.destination, move.piece))
    }

    private fun applyPromotions(move: Move, positions: List<Position>): List<Position> {
        if (move.piece.type == PAWN && move.promotedTo != null) {
            return positions
                .filter { it.square != move.destination }
                .plus(Position(move.destination, move.promotedTo))
        }
        return positions
    }

    private fun applyRookMovesForCastling(move: Move, positions: List<Position>): List<Position> {
        if (move.isKingCastle && move.piece.color == WHITE) {
            return positions
                .filter { it.square != H1 }
                .plus(Position(F1, WHITE_ROOK))
        } else if (move.isQueenCastle && move.piece.color == WHITE) {
            return positions
                .filter { it.square != A1 }
                .plus(Position(D1, WHITE_ROOK))
        } else if (move.isKingCastle && move.piece.color == BLACK) {
            return positions
                .filter { it.square != H8 }
                .plus(Position(F8, BLACK_ROOK))
        } else if (move.isQueenCastle && move.piece.color == BLACK) {
            return positions
                .filter { it.square != A8 }
                .plus(Position(D8, BLACK_ROOK))
        } else {
            return positions
        }
    }
}
