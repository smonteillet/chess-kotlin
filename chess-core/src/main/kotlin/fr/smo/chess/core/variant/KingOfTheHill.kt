package fr.smo.chess.core.variant

import fr.smo.chess.core.PieceType
import fr.smo.chess.core.Position
import fr.smo.chess.core.Square.*

data object KingOfTheHill : Variant() {
    override fun isVariantVictory(position: Position): Boolean {
        return hasKingReachedTheCenter(position) || position.isCheckMate()
    }

    private fun hasKingReachedTheCenter(position: Position): Boolean {
        return position.history.lastMove?.let { move ->
            move.piece.type == PieceType.KING && listOf(E4, E5, D4, D5).contains(move.destination)
        } ?: false
    }

    override fun isVariantDraw(position: Position): Boolean {
        return position.isDraw()
    }
}