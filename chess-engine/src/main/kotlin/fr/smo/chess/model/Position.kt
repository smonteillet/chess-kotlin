package fr.smo.chess.model

data class Position(val square: Square, val piece: Piece) {

    fun hasNotSameColorPiece(other: Position) : Boolean {
        return piece.color != other.piece.color
    }
}