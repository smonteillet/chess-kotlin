package fr.smo.chess.core.variant

import fr.smo.chess.core.Position

sealed class Variant {

    abstract fun isVariantVictory(position : Position) : Boolean

    abstract fun isVariantDraw(position : Position) : Boolean
}