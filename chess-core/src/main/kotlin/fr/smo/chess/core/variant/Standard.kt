package fr.smo.chess.core.variant

import fr.smo.chess.core.Position

data object Standard : Variant() {
    override fun isVariantVictory(position: Position): Boolean {
        return position.isCheckMate()
    }

    override fun isVariantDraw(position: Position): Boolean {
        return position.isDraw()
    }
}