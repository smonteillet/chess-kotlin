package fr.smo.chess.core

enum class Square(val file: File, val rank: Rank) {

    A1(File.A, Rank.FIRST),
    A2(File.A, Rank.SECOND),
    A3(File.A, Rank.THIRD),
    A4(File.A, Rank.FOURTH),
    A5(File.A, Rank.FIFTH),
    A6(File.A, Rank.SIXTH),
    A7(File.A, Rank.SEVENTH),
    A8(File.A, Rank.EIGHTH),

    B1(File.B, Rank.FIRST),
    B2(File.B, Rank.SECOND),
    B3(File.B, Rank.THIRD),
    B4(File.B, Rank.FOURTH),
    B5(File.B, Rank.FIFTH),
    B6(File.B, Rank.SIXTH),
    B7(File.B, Rank.SEVENTH),
    B8(File.B, Rank.EIGHTH),

    C1(File.C, Rank.FIRST),
    C2(File.C, Rank.SECOND),
    C3(File.C, Rank.THIRD),
    C4(File.C, Rank.FOURTH),
    C5(File.C, Rank.FIFTH),
    C6(File.C, Rank.SIXTH),
    C7(File.C, Rank.SEVENTH),
    C8(File.C, Rank.EIGHTH),

    D1(File.D, Rank.FIRST),
    D2(File.D, Rank.SECOND),
    D3(File.D, Rank.THIRD),
    D4(File.D, Rank.FOURTH),
    D5(File.D, Rank.FIFTH),
    D6(File.D, Rank.SIXTH),
    D7(File.D, Rank.SEVENTH),
    D8(File.D, Rank.EIGHTH),

    E1(File.E, Rank.FIRST),
    E2(File.E, Rank.SECOND),
    E3(File.E, Rank.THIRD),
    E4(File.E, Rank.FOURTH),
    E5(File.E, Rank.FIFTH),
    E6(File.E, Rank.SIXTH),
    E7(File.E, Rank.SEVENTH),
    E8(File.E, Rank.EIGHTH),

    F1(File.F, Rank.FIRST),
    F2(File.F, Rank.SECOND),
    F3(File.F, Rank.THIRD),
    F4(File.F, Rank.FOURTH),
    F5(File.F, Rank.FIFTH),
    F6(File.F, Rank.SIXTH),
    F7(File.F, Rank.SEVENTH),
    F8(File.F, Rank.EIGHTH),

    G1(File.G, Rank.FIRST),
    G2(File.G, Rank.SECOND),
    G3(File.G, Rank.THIRD),
    G4(File.G, Rank.FOURTH),
    G5(File.G, Rank.FIFTH),
    G6(File.G, Rank.SIXTH),
    G7(File.G, Rank.SEVENTH),
    G8(File.G, Rank.EIGHTH),

    H1(File.H, Rank.FIRST),
    H2(File.H, Rank.SECOND),
    H3(File.H, Rank.THIRD),
    H4(File.H, Rank.FOURTH),
    H5(File.H, Rank.FIFTH),
    H6(File.H, Rank.SIXTH),
    H7(File.H, Rank.SEVENTH),
    H8(File.H, Rank.EIGHTH);

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

    fun toTheVeryLeft(): Square = at(File.A, rank)!!

    override fun toString(): String = file.label + rank.label
}
