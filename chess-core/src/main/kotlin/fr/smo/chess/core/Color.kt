package fr.smo.chess.core

import fr.smo.chess.core.utils.ifTrue

enum class Color {
    WHITE, BLACK;

    fun opposite() : Color {
        return if (this == WHITE) {
            BLACK
        } else {
            WHITE
        }
    }

    fun getPawnAdvanceFunction(advanceTwice : Boolean = false) : (Square) -> Square? {
        return when(this) {
            WHITE -> { square : Square -> advanceTwice.ifTrue { square.up()?.up() } ?:square.up() }
            BLACK -> { square : Square -> advanceTwice.ifTrue { square.down()?.down() } ?:square.down() }
        }
    }

    fun getPawnDiagonalSquareFunction() : List<(Square) -> Square?> {
        return when(this) {
            WHITE -> listOf({ square : Square -> square.upLeft() }, { square : Square -> square.upRight() })
            BLACK -> listOf({ square : Square -> square.downLeft() }, { square : Square -> square.downRight() })
        }
    }

    fun getPawnStartingRank() : Rank {
        return when(this) {
            WHITE -> Rank.RANK_2
            BLACK -> Rank.RANK_7
        }
    }

    fun getPromotionRank() : Rank {
        return when(this) {
            WHITE -> Rank.RANK_8
            BLACK -> Rank.RANK_1
        }
    }
}