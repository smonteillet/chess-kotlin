package fr.smo.chess.core.algs

import fr.smo.chess.core.*
import fr.smo.chess.core.utils.Failure
import fr.smo.chess.core.utils.Success
import fr.smo.chess.core.utils.ifTrue

fun isChecked(kingColorThatMayBeChecked: Color, position: Position): Boolean {
    return hasAPseudoLegalMovesSatisfying(kingColorThatMayBeChecked.opposite(), position) { move ->
        move.capturedPiece?.type == PieceType.KING
    }
}