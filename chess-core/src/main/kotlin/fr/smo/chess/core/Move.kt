package fr.smo.chess.core


data class Move(
    val piece: Piece,
    val origin: Square,
    val destination: Square,
    val capturedPiece: Piece? = null,
    val promotedTo: Piece? = null,
    val isKingCastle: Boolean = false,
    val isQueenCastle: Boolean = false,
    val isCheck: Boolean = false,
) {
    val isCastle : Boolean
        get() = isQueenCastle || isKingCastle
}
