package fr.smo.chess.core

import fr.smo.chess.core.utils.ifTrue


data class PiecePosition(val square: Square, val piece: Piece) {
    val color: Color
        get() = this.piece.color

    val pieceType: PieceType
        get() = this.piece.type
}