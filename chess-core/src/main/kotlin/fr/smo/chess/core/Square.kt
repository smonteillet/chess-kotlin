package fr.smo.chess.core

import fr.smo.chess.core.File.*
import fr.smo.chess.core.Rank.*

enum class Square(val file: File, val rank: Rank) {

    A1(FILE_A, RANK_1),
    A2(FILE_A, RANK_2),
    A3(FILE_A, RANK_3),
    A4(FILE_A, RANK_4),
    A5(FILE_A, RANK_5),
    A6(FILE_A, RANK_6),
    A7(FILE_A, RANK_7),
    A8(FILE_A, RANK_8),

    B1(FILE_B, RANK_1),
    B2(FILE_B, RANK_2),
    B3(FILE_B, RANK_3),
    B4(FILE_B, RANK_4),
    B5(FILE_B, RANK_5),
    B6(FILE_B, RANK_6),
    B7(FILE_B, RANK_7),
    B8(FILE_B, RANK_8),

    C1(FILE_C, RANK_1),
    C2(FILE_C, RANK_2),
    C3(FILE_C, RANK_3),
    C4(FILE_C, RANK_4),
    C5(FILE_C, RANK_5),
    C6(FILE_C, RANK_6),
    C7(FILE_C, RANK_7),
    C8(FILE_C, RANK_8),

    D1(FILE_D, RANK_1),
    D2(FILE_D, RANK_2),
    D3(FILE_D, RANK_3),
    D4(FILE_D, RANK_4),
    D5(FILE_D, RANK_5),
    D6(FILE_D, RANK_6),
    D7(FILE_D, RANK_7),
    D8(FILE_D, RANK_8),

    E1(FILE_E, RANK_1),
    E2(FILE_E, RANK_2),
    E3(FILE_E, RANK_3),
    E4(FILE_E, RANK_4),
    E5(FILE_E, RANK_5),
    E6(FILE_E, RANK_6),
    E7(FILE_E, RANK_7),
    E8(FILE_E, RANK_8),

    F1(FILE_F, RANK_1),
    F2(FILE_F, RANK_2),
    F3(FILE_F, RANK_3),
    F4(FILE_F, RANK_4),
    F5(FILE_F, RANK_5),
    F6(FILE_F, RANK_6),
    F7(FILE_F, RANK_7),
    F8(FILE_F, RANK_8),

    G1(FILE_G, RANK_1),
    G2(FILE_G, RANK_2),
    G3(FILE_G, RANK_3),
    G4(FILE_G, RANK_4),
    G5(FILE_G, RANK_5),
    G6(FILE_G, RANK_6),
    G7(FILE_G, RANK_7),
    G8(FILE_G, RANK_8),

    H1(FILE_H, RANK_1),
    H2(FILE_H, RANK_2),
    H3(FILE_H, RANK_3),
    H4(FILE_H, RANK_4),
    H5(FILE_H, RANK_5),
    H6(FILE_H, RANK_6),
    H7(FILE_H, RANK_7),
    H8(FILE_H, RANK_8);

    companion object {

        private val squareMaps: Map<File, Map<Rank, Square>> = mapOf(
                FILE_A to mapOf(
                        RANK_1 to A1,
                        RANK_2 to A2,
                        RANK_3 to A3,
                        RANK_4 to A4,
                        RANK_5 to A5,
                        RANK_6 to A6,
                        RANK_7 to A7,
                        RANK_8 to A8
                ),
                FILE_B to mapOf(
                        RANK_1 to B1,
                        RANK_2 to B2,
                        RANK_3 to B3,
                        RANK_4 to B4,
                        RANK_5 to B5,
                        RANK_6 to B6,
                        RANK_7 to B7,
                        RANK_8 to B8
                ),
                FILE_C to mapOf(
                        RANK_1 to C1,
                        RANK_2 to C2,
                        RANK_3 to C3,
                        RANK_4 to C4,
                        RANK_5 to C5,
                        RANK_6 to C6,
                        RANK_7 to C7,
                        RANK_8 to C8
                ),
                FILE_D to mapOf(
                        RANK_1 to D1,
                        RANK_2 to D2,
                        RANK_3 to D3,
                        RANK_4 to D4,
                        RANK_5 to D5,
                        RANK_6 to D6,
                        RANK_7 to D7,
                        RANK_8 to D8
                ),
                FILE_E to mapOf(
                        RANK_1 to E1,
                        RANK_2 to E2,
                        RANK_3 to E3,
                        RANK_4 to E4,
                        RANK_5 to E5,
                        RANK_6 to E6,
                        RANK_7 to E7,
                        RANK_8 to E8
                ),
                FILE_F to mapOf(
                        RANK_1 to F1,
                        RANK_2 to F2,
                        RANK_3 to F3,
                        RANK_4 to F4,
                        RANK_5 to F5,
                        RANK_6 to F6,
                        RANK_7 to F7,
                        RANK_8 to F8
                ),
                FILE_G to mapOf(
                        RANK_1 to G1,
                        RANK_2 to G2,
                        RANK_3 to G3,
                        RANK_4 to G4,
                        RANK_5 to G5,
                        RANK_6 to G6,
                        RANK_7 to G7,
                        RANK_8 to G8
                ),
                FILE_H to mapOf(
                        RANK_1 to H1,
                        RANK_2 to H2,
                        RANK_3 to H3,
                        RANK_4 to H4,
                        RANK_5 to H5,
                        RANK_6 to H6,
                        RANK_7 to H7,
                        RANK_8 to H8
                ),
        )


        fun at(file: File?, rank: Rank?): Square? {
            return squareMaps[file]?.get(rank)
        }

        fun at(algebraicNotation: String): Square {
            return at(
                File.at(algebraicNotation[0].toString()),
                Rank.at(algebraicNotation[1].digitToInt())
            ) ?: throw IllegalArgumentException("Square $algebraicNotation does not exist")
        }
    }

    fun isLightSquare(): Boolean {
        return when (rank) {
            RANK_1, RANK_3, RANK_5, RANK_7 -> when (file) {
                FILE_B, FILE_D, FILE_F, FILE_H -> true
                FILE_A, FILE_C, FILE_E, FILE_G -> false
            }

            RANK_2, RANK_4, RANK_6, RANK_8 -> when (file) {
                FILE_B, FILE_D, FILE_F, FILE_H -> false
                FILE_A, FILE_C, FILE_E, FILE_G -> true
            }
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

    fun toTheVeryLeft(): Square = at(FILE_A, rank)!!

    override fun toString(): String = file.label + rank.label
}
