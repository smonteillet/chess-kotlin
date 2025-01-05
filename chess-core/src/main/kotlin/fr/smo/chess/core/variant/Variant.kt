package fr.smo.chess.core.variant

import fr.smo.chess.core.*
import fr.smo.chess.core.notation.STARTING_STANDARD_POSITION_FEN

sealed class Variant {

    abstract fun isVariantVictory(position : Position) : Boolean

    abstract fun isVariantDraw(position : Position) : Boolean

    open fun getStartingFenPosition(): String {
        return STARTING_STANDARD_POSITION_FEN
    }

    open fun initCastles(chessboard: Chessboard): Castles {
        return Standard.generateStartingCastles()
    }
}