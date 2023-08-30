package fr.smo.chess.core

enum class PieceType(val pgnNotation: String, val isPromotable : Boolean) {
    PAWN("", false),
    BISHOP("B", true),
    KNIGHT("N", true),
    QUEEN("Q", true),
    KING("K", false),
    ROOK("R", true);

    companion object {
        fun fromPGNNotation(pgn: String): PieceType? = entries.firstOrNull { it.pgnNotation == pgn }
    }
}