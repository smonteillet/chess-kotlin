package fr.smo.chess.core

enum class Square(val file: File, val rank: Rank) {

    A1(File.FILE_A, Rank.RANK_1),
    A2(File.FILE_A, Rank.RANK_2),
    A3(File.FILE_A, Rank.RANK_3),
    A4(File.FILE_A, Rank.RANK_4),
    A5(File.FILE_A, Rank.RANK_5),
    A6(File.FILE_A, Rank.RANK_6),
    A7(File.FILE_A, Rank.RANK_7),
    A8(File.FILE_A, Rank.RANK_8),

    B1(File.FILE_B, Rank.RANK_1),
    B2(File.FILE_B, Rank.RANK_2),
    B3(File.FILE_B, Rank.RANK_3),
    B4(File.FILE_B, Rank.RANK_4),
    B5(File.FILE_B, Rank.RANK_5),
    B6(File.FILE_B, Rank.RANK_6),
    B7(File.FILE_B, Rank.RANK_7),
    B8(File.FILE_B, Rank.RANK_8),

    C1(File.FILE_C, Rank.RANK_1),
    C2(File.FILE_C, Rank.RANK_2),
    C3(File.FILE_C, Rank.RANK_3),
    C4(File.FILE_C, Rank.RANK_4),
    C5(File.FILE_C, Rank.RANK_5),
    C6(File.FILE_C, Rank.RANK_6),
    C7(File.FILE_C, Rank.RANK_7),
    C8(File.FILE_C, Rank.RANK_8),

    D1(File.FILE_D, Rank.RANK_1),
    D2(File.FILE_D, Rank.RANK_2),
    D3(File.FILE_D, Rank.RANK_3),
    D4(File.FILE_D, Rank.RANK_4),
    D5(File.FILE_D, Rank.RANK_5),
    D6(File.FILE_D, Rank.RANK_6),
    D7(File.FILE_D, Rank.RANK_7),
    D8(File.FILE_D, Rank.RANK_8),

    E1(File.FILE_E, Rank.RANK_1),
    E2(File.FILE_E, Rank.RANK_2),
    E3(File.FILE_E, Rank.RANK_3),
    E4(File.FILE_E, Rank.RANK_4),
    E5(File.FILE_E, Rank.RANK_5),
    E6(File.FILE_E, Rank.RANK_6),
    E7(File.FILE_E, Rank.RANK_7),
    E8(File.FILE_E, Rank.RANK_8),

    F1(File.FILE_F, Rank.RANK_1),
    F2(File.FILE_F, Rank.RANK_2),
    F3(File.FILE_F, Rank.RANK_3),
    F4(File.FILE_F, Rank.RANK_4),
    F5(File.FILE_F, Rank.RANK_5),
    F6(File.FILE_F, Rank.RANK_6),
    F7(File.FILE_F, Rank.RANK_7),
    F8(File.FILE_F, Rank.RANK_8),

    G1(File.FILE_G, Rank.RANK_1),
    G2(File.FILE_G, Rank.RANK_2),
    G3(File.FILE_G, Rank.RANK_3),
    G4(File.FILE_G, Rank.RANK_4),
    G5(File.FILE_G, Rank.RANK_5),
    G6(File.FILE_G, Rank.RANK_6),
    G7(File.FILE_G, Rank.RANK_7),
    G8(File.FILE_G, Rank.RANK_8),

    H1(File.FILE_H, Rank.RANK_1),
    H2(File.FILE_H, Rank.RANK_2),
    H3(File.FILE_H, Rank.RANK_3),
    H4(File.FILE_H, Rank.RANK_4),
    H5(File.FILE_H, Rank.RANK_5),
    H6(File.FILE_H, Rank.RANK_6),
    H7(File.FILE_H, Rank.RANK_7),
    H8(File.FILE_H, Rank.RANK_8);

    companion object {
        fun at(file: File?, rank: Rank?): Square? {
            return Square.values().firstOrNull { it.rank == rank && it.file == file }
        }

        fun at(algebraicNotation: String): Square {
            return at(
                File.at(algebraicNotation[0].toString()),
                Rank.at(algebraicNotation[1].digitToInt())
            ) ?: throw IllegalArgumentException("Square $algebraicNotation does not exist")
        }
    }

    fun withRank(newRank: Rank): Square? = at(file, newRank)

    fun upLeft(): Square? = at(file.left(), rank.top())

    fun upRight(): Square? = at(file.right(), rank.top())

    fun downLeft(): Square? = at(file.left(), rank.bottom())

    fun downRight(): Square? = at(file.right(), rank.bottom())

    fun up(): Square? = at(file, rank.top())

    fun down(): Square? = at(file, rank.bottom())

    fun left(): Square? = at(file.left(), rank)

    fun right(): Square? = at(file.right(), rank)

    fun toTheVeryLeft(): Square = at(File.FILE_A, rank)!!

    override fun toString(): String = file.label + rank.label
}
