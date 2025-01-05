package fr.smo.chess.core.variant

import fr.smo.chess.core.*
import fr.smo.chess.core.Square.*

data object Standard : Variant() {
    override fun isVariantVictory(position: Position): Boolean {
        return position.isCheckMate()
    }

    override fun isVariantDraw(position: Position): Boolean {
        return position.isDraw()
    }

    fun generateStartingCastles(): Castles {
        return Castles(
            castles = listOf(
                Castle(
                    color = Color.WHITE,
                    castleType = CastleType.SHORT,
                    kingStartSquare = E1,
                    rookStartSquare = H1,
                    kingCastlingPathSquares = listOf(E1, F1, G1),
                    rookCastlingPathSquares = listOf(H1, G1, F1, E1),
                ),
                Castle(
                    color = Color.WHITE,
                    castleType = CastleType.LONG,
                    kingStartSquare = E1,
                    rookStartSquare = A1,
                    kingCastlingPathSquares = listOf(E1, D1, C1),
                    rookCastlingPathSquares = listOf(A1, B1, C1, D1),
                ),
                Castle(
                    color = Color.BLACK,
                    castleType = CastleType.SHORT,
                    kingStartSquare = E8,
                    rookStartSquare = H8,
                    kingCastlingPathSquares = listOf(E8, F8, G8),
                    rookCastlingPathSquares = listOf(H8, G8, F8, E8),
                ),
                Castle(
                    color = Color.BLACK,
                    castleType = CastleType.LONG,
                    kingStartSquare = E8,
                    rookStartSquare = A8,
                    kingCastlingPathSquares = listOf(E8, D8, C8),
                    rookCastlingPathSquares = listOf(A8, B8, C8, D8),
                ),
            )
        )
    }
}