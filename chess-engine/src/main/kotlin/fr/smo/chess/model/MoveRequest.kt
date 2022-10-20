package fr.smo.chess.model

data class MoveRequest(
    val from: Square,
    val destination: Square,
    val promotedPiece: Piece.Type?,
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

    companion object {
        fun moveRequest(from : String, destination : String, promotedTo : Piece.Type? = null) : MoveRequest =
            MoveRequest(Square.square(from), Square.square(destination), promotedTo)
    }
}
