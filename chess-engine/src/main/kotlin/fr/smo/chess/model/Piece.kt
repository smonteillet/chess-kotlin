package fr.smo.chess.model

import fr.smo.chess.model.Color.BLACK
import fr.smo.chess.model.Color.WHITE
import fr.smo.chess.model.Piece.Type.*

data class Piece(
    val type: Type, val color: Color,
) {
    enum class Type {
        PAWN,
        BISHOP,
        KNIGHT,
        QUEEN,
        KING,
        ROOK,
    }

    companion object {
        private val cache : MutableMap<Color, MutableMap<Type, Piece>> = mutableMapOf()

        init {
            cache[WHITE] = mutableMapOf()
            cache[BLACK] = mutableMapOf()
            cache[WHITE]!![PAWN] = Piece(PAWN,WHITE)
            cache[BLACK]!![PAWN] = Piece(PAWN, BLACK)
            cache[WHITE]!![BISHOP] = Piece(BISHOP,WHITE)
            cache[BLACK]!![BISHOP] = Piece(BISHOP, BLACK)
            cache[WHITE]!![ROOK] = Piece(ROOK,WHITE)
            cache[BLACK]!![ROOK] = Piece(ROOK, BLACK)
            cache[WHITE]!![KNIGHT] = Piece(KNIGHT,WHITE)
            cache[BLACK]!![KNIGHT] = Piece(KNIGHT, BLACK)
            cache[WHITE]!![QUEEN] = Piece(QUEEN,WHITE)
            cache[BLACK]!![QUEEN] = Piece(QUEEN, BLACK)
            cache[WHITE]!![KING] = Piece(KING,WHITE)
            cache[BLACK]!![KING] = Piece(KING, BLACK)
        }

        fun piece(type: Type, color: Color) : Piece {
            return cache[color]!![type]!!
        }
        fun whitePawn() = cache[WHITE]!![PAWN]!!
        fun blackPawn() = cache[BLACK]!![PAWN]!!
        fun whiteBishop() = cache[WHITE]!![BISHOP]!!
        fun blackBishop() = cache[BLACK]!![BISHOP]!!
        fun whiteRook() = cache[WHITE]!![ROOK]!!
        fun blackRook() = cache[BLACK]!![ROOK]!!
        fun whiteKnight() = cache[WHITE]!![KNIGHT]!!
        fun blackKnight() = cache[BLACK]!![KNIGHT]!!
        fun whiteQueen() = cache[WHITE]!![QUEEN]!!
        fun blackQueen() = cache[BLACK]!![QUEEN]!!
        fun whiteKing() = cache[WHITE]!![KING]!!
        fun blackKing() = cache[BLACK]!![KING]!!
    }

}
