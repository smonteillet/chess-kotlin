package fr.smo.chess.core

data class MoveRequest(
    val from: Square,
    val destination: Square,
    val promotedPiece: PieceType? = null,
) {
    init {
        require(
            promotedPiece == null || promotedPiece.isPromotable
        )
    }

}
