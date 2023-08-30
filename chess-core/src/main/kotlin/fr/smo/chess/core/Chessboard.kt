package fr.smo.chess.core

data class Chessboard(
    val piecesOnBoard: List<PiecePosition> = listOf(),
) {

    fun isPiecePresentAtAndHasColor(square: Square, color: Color): Boolean = getPositionAt(square)?.piece?.color == color

    fun getPositionAt(square: Square): PiecePosition? {
        return this.piecesOnBoard.firstOrNull { it.square == square }
    }

    fun getAllPseudoLegalMovesForColor(color: Color, game: Game): List<Move> {
        return game.chessboard.piecesOnBoard
            .filter { it.piece.color == color }
            .flatMap { it.getAllPseudoLegalMoves(game) }
    }

    fun applyMoveOnBoard(move: Move, enPassantTargetSquare: Square?): Chessboard = Chessboard(
        piecesOnBoard = piecesOnBoard
            .filter { it.square != move.origin }
            .filter { it.square != move.destination }
            .filter { it.square != getOpponentPawnThatHasBeenEnPassant(move, enPassantTargetSquare) }
            .plus(PiecePosition(move.destination, move.piece))
    )

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

    fun applyPromotions(move: Move): Chessboard {
        if (move.piece.type == PieceType.PAWN && move.promotedTo != null) {
            return Chessboard(
                piecesOnBoard = piecesOnBoard
                    .filter { it.square != move.destination }
                    .plus(PiecePosition(move.destination, move.promotedTo))
            )
        }
        return this
    }

    fun applyRookMovesForCastling(move: Move): Chessboard {
        val newPiecesOnBoard = if (move.isKingCastle && move.piece.color == Color.WHITE) {
            piecesOnBoard
                .filter { it.square != Square.H1 }
                .plus(PiecePosition(Square.F1, Piece.WHITE_ROOK))
        } else if (move.isQueenCastle && move.piece.color == Color.WHITE) {
            piecesOnBoard
                .filter { it.square != Square.A1 }
                .plus(PiecePosition(Square.D1, Piece.WHITE_ROOK))
        } else if (move.isKingCastle && move.piece.color == Color.BLACK) {
            piecesOnBoard
                .filter { it.square != Square.H8 }
                .plus(PiecePosition(Square.F8, Piece.BLACK_ROOK))
        } else if (move.isQueenCastle && move.piece.color == Color.BLACK) {
            piecesOnBoard
                .filter { it.square != Square.A8 }
                .plus(PiecePosition(Square.D8, Piece.BLACK_ROOK))
        } else {
            piecesOnBoard
        }
        return Chessboard(newPiecesOnBoard)
    }

}
