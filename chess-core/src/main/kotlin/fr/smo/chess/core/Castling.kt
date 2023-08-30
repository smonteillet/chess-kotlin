package fr.smo.chess.core

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
        return isWhiteKingCastlePossible && move.piece != Piece.WHITE_KING && move.origin != Square.H1 && move.destination != Square.H1
    }

    private fun isWhiteQueenSideCastlingStillPossible(move: Move): Boolean {
        return isWhiteQueenCastlePossible && move.piece != Piece.WHITE_KING && move.origin != Square.A1 && move.destination != Square.A1
    }

    private fun isBlackKingSideCastlingStillPossible(move: Move): Boolean {
        return isBlackKingCastlePossible && move.piece != Piece.BLACK_KING && move.origin != Square.H8 && move.destination != Square.H8
    }

    private fun isBlackQueenSideCastlingStillPossible(move: Move): Boolean {
        return isBlackQueenCastlePossible && move.piece != Piece.BLACK_KING && move.origin != Square.A8 && move.destination != Square.A8
    }

}