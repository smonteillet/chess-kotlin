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

    val label: String = value.toString()

    companion object {

        fun at(index: Int) = entries.firstOrNull { it.value == index }
            ?: throw IllegalArgumentException("Rank index shall be between 1 and 8")
    }

    fun top(): Rank? = when (this) {
        RANK_1 -> RANK_2
        RANK_2 -> RANK_3
        RANK_3 -> RANK_4
        RANK_4 -> RANK_5
        RANK_5 -> RANK_6
        RANK_6 -> RANK_7
        RANK_7 -> RANK_8
        else -> null
    }

    fun bottom(): Rank? = when (this) {
        RANK_8 -> RANK_7
        RANK_7 -> RANK_6
        RANK_6 -> RANK_5
        RANK_5 -> RANK_4
        RANK_4 -> RANK_3
        RANK_3 -> RANK_2
        RANK_2 -> RANK_1
        else -> null
    }
}