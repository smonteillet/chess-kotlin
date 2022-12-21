package fr.smo.chess.core

data class Chessboard(
    val piecesOnBoard: List<Position> = listOf(),
) {

    fun isPiecePresentAtAndHasColor(square: Square, color: Color): Boolean = getPositionAt(square)?.piece?.color == color

    fun getPositionAt(square: Square): Position? {
        return this.piecesOnBoard.firstOrNull { it.square == square }
    }

    fun applyMoveOnBoard(move: Move, enPassantTargetSquare: Square?): Chessboard {
        val opponentPawnThatHasBeenEnPassant =
            if (move.destination == enPassantTargetSquare && move.piece.type == PieceType.PAWN) {
                if (move.piece.color == Color.WHITE) {
                    enPassantTargetSquare.down()!!
                } else {
                    enPassantTargetSquare.up()!!
                }
            } else {
                null
            }
        return Chessboard(
            piecesOnBoard = piecesOnBoard
                .filter { it.square != move.from }
                .filter { it.square != move.destination }
                .filter { it.square != opponentPawnThatHasBeenEnPassant }
                .plus(Position(move.destination, move.piece))
        )
    }

    fun applyPromotions(move: Move): Chessboard {
        if (move.piece.type == PieceType.PAWN && move.promotedTo != null) {
            return Chessboard(
                piecesOnBoard = piecesOnBoard
                    .filter { it.square != move.destination }
                    .plus(Position(move.destination, move.promotedTo))
            )
        }
        return this
    }

    fun applyRookMovesForCastling(move: Move): Chessboard {
        val newPiecesOnBoard = if (move.isKingCastle && move.piece.color == Color.WHITE) {
            piecesOnBoard
                .filter { it.square != Square.H1 }
                .plus(Position(Square.F1, Piece.WHITE_ROOK))
        } else if (move.isQueenCastle && move.piece.color == Color.WHITE) {
            piecesOnBoard
                .filter { it.square != Square.A1 }
                .plus(Position(Square.D1, Piece.WHITE_ROOK))
        } else if (move.isKingCastle && move.piece.color == Color.BLACK) {
            piecesOnBoard
                .filter { it.square != Square.H8 }
                .plus(Position(Square.F8, Piece.BLACK_ROOK))
        } else if (move.isQueenCastle && move.piece.color == Color.BLACK) {
            piecesOnBoard
                .filter { it.square != Square.A8 }
                .plus(Position(Square.D8, Piece.BLACK_ROOK))
        } else {
            piecesOnBoard
        }
        return Chessboard(newPiecesOnBoard)
    }

}
