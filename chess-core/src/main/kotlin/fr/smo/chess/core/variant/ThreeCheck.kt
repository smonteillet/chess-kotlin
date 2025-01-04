package fr.smo.chess.core.variant

import fr.smo.chess.core.Position

data object ThreeCheck : Variant() {
    override fun isVariantVictory(position: Position): Boolean {
        return hasAtLeastThreeChecks(position) || position.isCheckMate()
    }

    override fun isVariantDraw(position: Position): Boolean {
        return position.isDraw()
    }

    private fun hasAtLeastThreeChecks(position: Position): Boolean {
        val lastMovedColor = position.history.lastMovedColor!!
        return position.history.moves
            .filter { it.piece.color == lastMovedColor }
            .count { it.isCheck } >= 3
    }
}