package fr.smo.chess.core

import fr.smo.chess.core.Color.BLACK
import fr.smo.chess.core.Color.WHITE

data class Castling(
    val isWhiteKingCastlePossible: Boolean = true,
    val isWhiteQueenCastlePossible: Boolean = true,
    val isBlackKingCastlePossible: Boolean = true,
    val isBlackQueenCastlePossible: Boolean = true,
) {
    val isCastlingPossibleForWhite: Boolean = isWhiteKingCastlePossible || isWhiteQueenCastlePossible
    val isCastlingPossibleForBlack: Boolean = isBlackKingCastlePossible || isBlackQueenCastlePossible

    fun updateCastlingAfterMove(move: Move): Castling {
        return Castling(
            isBlackKingCastlePossible = isBlackKingSideCastlingStillPossible(move),
            isBlackQueenCastlePossible = isBlackQueenSideCastlingStillPossible(move),
            isWhiteQueenCastlePossible = isWhiteQueenSideCastlingStillPossible(move),
            isWhiteKingCastlePossible = isWhiteKingSideCastlingStillPossible(move),
        )
    }

    private fun isWhiteKingSideCastlingStillPossible(move: Move): Boolean {
        return isWhiteKingCastlePossible &&
                if (move.piece.color == BLACK) {
                    ((move.capturedPiece != null && move.destination != Square.H1) || move.capturedPiece == null)
                } else {
                    move.piece != Piece.WHITE_KING && (move.piece != Piece.WHITE_ROOK || move.from != Square.H1)
                }
    }

    private fun isWhiteQueenSideCastlingStillPossible(move: Move): Boolean {
        return isWhiteQueenCastlePossible &&
                if (move.piece.color == BLACK) {
                    ((move.capturedPiece != null && move.destination != Square.A1) || move.capturedPiece == null)
                } else {
                    move.piece != Piece.WHITE_KING && (move.piece != Piece.WHITE_ROOK || move.from != Square.A1)
                }
    }

    private fun isBlackKingSideCastlingStillPossible(move: Move): Boolean {
        return isBlackKingCastlePossible &&
                if (move.piece.color == WHITE) {
                    ((move.capturedPiece != null && move.destination != Square.H8) || move.capturedPiece == null)
                } else {
                    move.piece != Piece.BLACK_KING && (move.piece != Piece.BLACK_ROOK || move.from != Square.H8)
                }
    }

    private fun isBlackQueenSideCastlingStillPossible(move: Move): Boolean {
        return isBlackQueenCastlePossible &&
                if (move.piece.color == WHITE) {
                    ((move.capturedPiece != null && move.destination != Square.A8) || move.capturedPiece == null)
                } else {
                    move.piece != Piece.BLACK_KING && (move.piece != Piece.BLACK_ROOK || move.from != Square.A8)
                }
    }
}