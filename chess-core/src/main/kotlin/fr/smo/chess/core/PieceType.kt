package fr.smo.chess.core

enum class PieceType(val pgnNotation: String) {
    PAWN(""),
    BISHOP("B"),
    KNIGHT("N"),
    QUEEN("Q"),
    KING("K"),
    ROOK("R");

    companion object {
        fun fromPGNNotation(pgn: String): PieceType? =
            PieceType.values()
                .firstOrNull { it.pgnNotation == pgn }
    }
}