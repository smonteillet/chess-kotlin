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
                    squaresBetweenKingAndRook = listOf(F1, G1),
                    kingCastlingPathSquares = listOf(E1, F1, G1),
                ),
                Castle(
                    color = Color.WHITE,
                    castleType = CastleType.LONG,
                    kingStartSquare = E1,
                    rookStartSquare = A1,
                    squaresBetweenKingAndRook = listOf(D1, C1, B1),
                    kingCastlingPathSquares = listOf(E1, D1, C1),
                ),
                Castle(
                    color = Color.BLACK,
                    castleType = CastleType.SHORT,
                    kingStartSquare = E8,
                    rookStartSquare = H8,
                    squaresBetweenKingAndRook = listOf(F8, G8),
                    kingCastlingPathSquares = listOf(E8, F8, G8),
                ),
                Castle(
                    color = Color.BLACK,
                    castleType = CastleType.LONG,
                    kingStartSquare = E8,
                    rookStartSquare = A8,
                    squaresBetweenKingAndRook = listOf(D8, C8, B8),
                    kingCastlingPathSquares = listOf(E8, D8, C8),
                ),
            )
        )
    }
}