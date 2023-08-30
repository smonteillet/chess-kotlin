package fr.smo.chess.core

data class MoveCommand(
    val origin: Square,
    val destination: Square,
    val promotedPiece: PieceType? = null,
) {
    init {
        require(
            promotedPiece == null || promotedPiece.isPromotable
        )
    }

}
