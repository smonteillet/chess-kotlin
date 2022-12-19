package fr.smo.chess.core

enum class Rank(val value: Int) {

    RANK_1(1),
    RANK_2(2),
    RANK_3(3),
    RANK_4(4),
    RANK_5(5),
    RANK_6(6),
    RANK_7(7),
    RANK_8(8);

    val label : String = value.toString()

    companion object {

        fun at(index: Int) = Rank.values().firstOrNull { it.value == index }
            ?: throw IllegalArgumentException("Rank index shall be between 1 and 8")
    }

    fun top(): Rank? = if (value + 1 <= RANK_8.value) at(value + 1) else null
    fun bottom(): Rank? = if (value - 1 >= RANK_1.value) at(value - 1) else null
}