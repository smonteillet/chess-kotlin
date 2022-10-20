package fr.smo.chess.model

import java.lang.IllegalArgumentException

data class Square(
    private val column: Int, val row: Int
) {
    private val algebraicNotation : String
        get() = (('a' - 1) + column).toString() + row

    fun getColumn() : String {
        return when(column) {
            1 -> "A"
            2 -> "B"
            3 -> "C"
            4 -> "D"
            5 -> "E"
            6 -> "F"
            7 -> "G"
            8 -> "H"
            else -> throw IllegalStateException("Column index is $column")
        }
    }

    companion object {
        private val cache = mutableMapOf<Int, Square>()

        init {
            for (r in 1..8) {
                for (c in 1..8) {
                    cache[r * 10 + c] = Square(c, r)
                }
            }
        }

        private fun square(column: Int, row: Int): Square? {
            return cache[row * 10 + column]
        }

        fun square(algebraicNotation: String): Square {
            return square(
                algebraicNotation[0] - 'a' + 1,
                algebraicNotation[1].digitToInt()
            ) ?: throw IllegalArgumentException("Square $algebraicNotation does not exist")
        }

    }

    fun withRow(newRow : Int) :Square? {
        return square(column, newRow)
    }

    fun upLeft(): Square? {
        return square(column - 1, row + 1)
    }

    fun upRight(): Square? {
        return square(column + 1, row + 1)
    }

    fun downLeft(): Square? {
        return square(column - 1, row - 1)
    }

    fun downRight(): Square? {
        return square(column + 1, row - 1)
    }

    fun up(): Square? {
        return square(column, row + 1)
    }

    fun down(): Square? {
        return square(column, row - 1)
    }

    fun left(): Square? {
        return square(column - 1, row)
    }

    fun right(): Square? {
        return square(column + 1, row)
    }

    fun toTheVeryLeft() : Square {
        return square(1, row)!!
    }

    override fun toString(): String {
       return algebraicNotation
    }
}
