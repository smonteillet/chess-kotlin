package fr.smo.chess.core.algs

import fr.smo.chess.core.Color
import fr.smo.chess.core.PieceType
import fr.smo.chess.core.Position

fun isChecked(kingColorThatMayBeChecked: Color, position: Position): Boolean {
    return hasAPseudoLegalMovesSatisfying(kingColorThatMayBeChecked.opposite(), position) { move ->
        move.capturedPiece?.type == PieceType.KING
    }
}