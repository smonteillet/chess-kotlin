package fr.smo.chess.model


enum class Square(val rank: Rank, val file: File) {
    A1(Rank.FIRST, File.A),
    A2(Rank.SECOND, File.A),
    A3(Rank.THIRD, File.A),
    A4(Rank.FOURTH, File.A),
    A5(Rank.FIFTH, File.A),
    A6(Rank.SIXTH, File.A),
    A7(Rank.SEVENTH, File.A),
    A8(Rank.EIGHTH, File.A),

    B1(Rank.FIRST, File.B),
    B2(Rank.SECOND, File.B),
    B3(Rank.THIRD, File.B),
    B4(Rank.FOURTH, File.B),
    B5(Rank.FIFTH, File.B),
    B6(Rank.SIXTH, File.B),
    B7(Rank.SEVENTH, File.B),
    B8(Rank.EIGHTH, File.B),

    C1(Rank.FIRST, File.C),
    C2(Rank.SECOND, File.C),
    C3(Rank.THIRD, File.C),
    C4(Rank.FOURTH, File.C),
    C5(Rank.FIFTH, File.C),
    C6(Rank.SIXTH, File.C),
    C7(Rank.SEVENTH, File.C),
    C8(Rank.EIGHTH, File.C),

    D1(Rank.FIRST, File.D),
    D2(Rank.SECOND, File.D),
    D3(Rank.THIRD, File.D),
    D4(Rank.FOURTH, File.D),
    D5(Rank.FIFTH, File.D),
    D6(Rank.SIXTH, File.D),
    D7(Rank.SEVENTH, File.D),
    D8(Rank.EIGHTH, File.D),

    E1(Rank.FIRST, File.E),
    E2(Rank.SECOND, File.E),
    E3(Rank.THIRD, File.E),
    E4(Rank.FOURTH, File.E),
    E5(Rank.FIFTH, File.E),
    E6(Rank.SIXTH, File.E),
    E7(Rank.SEVENTH, File.E),
    E8(Rank.EIGHTH, File.E),

    F1(Rank.FIRST, File.F),
    F2(Rank.SECOND, File.F),
    F3(Rank.THIRD, File.F),
    F4(Rank.FOURTH, File.F),
    F5(Rank.FIFTH, File.F),
    F6(Rank.SIXTH, File.F),
    F7(Rank.SEVENTH, File.F),
    F8(Rank.EIGHTH, File.F),

    G1(Rank.FIRST, File.G),
    G2(Rank.SECOND, File.G),
    G3(Rank.THIRD, File.G),
    G4(Rank.FOURTH, File.G),
    G5(Rank.FIFTH, File.G),
    G6(Rank.SIXTH, File.G),
    G7(Rank.SEVENTH, File.G),
    G8(Rank.EIGHTH, File.G),

    H1(Rank.FIRST, File.H),
    H2(Rank.SECOND, File.H),
    H3(Rank.THIRD, File.H),
    H4(Rank.FOURTH, File.H),
    H5(Rank.FIFTH, File.H),
    H6(Rank.SIXTH, File.H),
    H7(Rank.SEVENTH, File.H),
    H8(Rank.EIGHTH, File.H);


    companion object {
        fun at(rank: Rank?, file: File?): Square? {
            return Square.values().firstOrNull { it.rank == rank && it.file == file }
        }

        fun at(algebraicNotation: String): Square {
            return at(Rank.at(algebraicNotation[1].digitToInt()), File.at(algebraicNotation[0].toString()))
                ?: throw IllegalArgumentException("Square $algebraicNotation does not exist")
        }
    }

    fun withRank(newRank: Rank): Square? {
        return at(newRank, file)
    }

    fun upLeft(): Square? {
        return at(rank.top(), file.left())
    }

    fun upRight(): Square? {
        return at(rank.top(), file.right())
    }

    fun downLeft(): Square? {
        return at(rank.bottom(), file.left())
    }

    fun downRight(): Square? {
        return at(rank.bottom(), file.right())
    }

    fun up(): Square? {
        return at(rank.top(), file)
    }

    fun down(): Square? {
        return at(rank.bottom(), file)
    }

    fun left(): Square? {
        return at(rank, file.left())
    }

    fun right(): Square? {
        return at(rank, file.right())
    }

    fun toTheVeryLeft(): Square {
        return at(rank, File.A)!!
    }


    private val algebraicNotation: String = file.label + rank.label


    override fun toString(): String {
        return algebraicNotation
    }
}
