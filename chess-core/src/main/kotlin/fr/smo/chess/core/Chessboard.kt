package fr.smo.chess.core

data class Chessboard(
    private val piecesOnBoard: Map<Square, Piece>,
) {

    fun isPiecePresentAtAndHasColor(square: Square, color: Color): Boolean = getPieceAt(square)?.color == color

    fun isPiecePresentAt(square: Square): Boolean = getPieceAt(square) != null

    fun hasNoPiecePresentAt(square: Square): Boolean = getPieceAt(square) == null

    fun getPieceAt(square: Square): Piece? = piecesOnBoard[square]

    fun getPiecePositions(color: Color) = getPiecePositions().filter { it.color == color }

    fun getPiecePositions() = piecesOnBoard.map { PiecePosition(square = it.key, piece = it.value) }

    fun count(predicate: (Map.Entry<Square, Piece>) -> Boolean): Int = piecesOnBoard.count(predicate)

    fun numberOfRemainingPieces() = piecesOnBoard.size

    fun hasAtLeastOnePieceAt(squares: List<Square>): Boolean = squares.any { getPieceAt(it) != null }

    fun applyMove(move: Move, enPassantTargetSquare: Square?): Chessboard {
        val newBoard = piecesOnBoard - move.origin - move.destination + (move.destination to move.piece)
        return Chessboard(piecesOnBoard = newBoard)
            .removeEnPassantPawnIfNecessary(move, enPassantTargetSquare)
            .applyPromotionIfNecessary(move)
            .applyRookMovesAfterCastleIfNecessary(move)
    }

    private fun removeEnPassantPawnIfNecessary(move: Move, enPassantTargetSquare: Square?): Chessboard {
        return getOpponentPawnThatHasBeenEnPassant(move, enPassantTargetSquare)?.let { enPassantPawn ->
            Chessboard(piecesOnBoard = piecesOnBoard - enPassantPawn)
        } ?: this
    }

    private fun getOpponentPawnThatHasBeenEnPassant(move: Move, enPassantTargetSquare: Square?) =
        if (move.destination == enPassantTargetSquare && move.piece.type == PieceType.PAWN) {
            if (move.piece.color == Color.WHITE) {
                enPassantTargetSquare.down()!!
            } else {
                enPassantTargetSquare.up()!!
            }
        } else {
            null
        }

    private fun applyPromotionIfNecessary(move: Move): Chessboard {
        if (move.piece.type == PieceType.PAWN && move.promotedTo != null) {
            return Chessboard(
                piecesOnBoard = piecesOnBoard - move.destination + (move.destination to move.promotedTo)
            )
        }
        return this
    }

    private fun applyRookMovesAfterCastleIfNecessary(move: Move): Chessboard {
        val newPiecesOnBoard = if (move.isKingCastle && move.piece.color == Color.WHITE) {
            piecesOnBoard - Square.H1 + (Square.F1 to Piece.WHITE_ROOK)
        } else if (move.isQueenCastle && move.piece.color == Color.WHITE) {
            piecesOnBoard - Square.A1 + (Square.D1 to Piece.WHITE_ROOK)
        } else if (move.isKingCastle && move.piece.color == Color.BLACK) {
            piecesOnBoard - Square.H8 + (Square.F8 to Piece.BLACK_ROOK)
        } else if (move.isQueenCastle && move.piece.color == Color.BLACK) {
            piecesOnBoard - Square.A8 + (Square.D8 to Piece.BLACK_ROOK)
        } else {
            piecesOnBoard
        }
        return Chessboard(newPiecesOnBoard)
    }

}
