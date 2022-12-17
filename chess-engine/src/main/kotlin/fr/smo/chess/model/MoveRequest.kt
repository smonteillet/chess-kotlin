package fr.smo.chess.model

data class MoveRequest(
    val from: Square,
    val destination: Square,
    val promotedPiece: Piece.Type? = null,
) {
    init {
        require(
            promotedPiece == null ||
                    promotedPiece == Piece.Type.QUEEN ||
                    promotedPiece == Piece.Type.BISHOP ||
                    promotedPiece == Piece.Type.ROOK ||
                    promotedPiece == Piece.Type.KNIGHT
        )
    }

}
