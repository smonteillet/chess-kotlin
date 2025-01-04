package fr.smo.chess.core.variant

import fr.smo.chess.core.*
import fr.smo.chess.core.Square.*
import fr.smo.chess.core.notation.STARTING_STANDARD_POSITION_FEN

sealed class Variant {

    abstract fun isVariantVictory(position : Position) : Boolean

    abstract fun isVariantDraw(position : Position) : Boolean

    open fun getStartingFenPosition(): String {
        return STARTING_STANDARD_POSITION_FEN
    }

    open fun initCastles(chessboard: Chessboard): Castles {
        return Castles(
            castles = listOf(
                Castle(color = Color.WHITE, castleType = CastleType.SHORT, kingStartSquare = E1, rookStartSquare = H1),
                Castle(color = Color.WHITE, castleType = CastleType.LONG, kingStartSquare = E1, rookStartSquare = A1),
                Castle(color = Color.BLACK, castleType = CastleType.SHORT, kingStartSquare = E8, rookStartSquare = H8),
                Castle(color = Color.BLACK, castleType = CastleType.LONG, kingStartSquare = E8, rookStartSquare = A8),
            )
        )
    }
}