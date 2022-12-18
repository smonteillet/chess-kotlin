package fr.smo.chess.core

enum class Rank(val value: Int) {

    FIRST(1),
    SECOND(2),
    THIRD(3),
    FOURTH(4),
    FIFTH(5),
    SIXTH(6),
    SEVENTH(7),
    EIGHTH(8);

    val label : String = value.toString()

    companion object {

        fun at(index: Int) = Rank.values().firstOrNull { it.value == index }
            ?: throw IllegalArgumentException("Rank index shall be between 1 and 8")
    }

    fun top(): Rank? = if (value + 1 <= EIGHTH.value) at(value + 1) else null
    fun bottom(): Rank? = if (value - 1 >= FIRST.value) at(value - 1) else null
}