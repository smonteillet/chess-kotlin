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
        return position.history.lastMovedColor?.let { lastMovedColor ->
            position.history.moves
                .filter { it.piece.color == lastMovedColor }
                .count { it.isCheck } >= 3
        } ?: false
    }
}