package fr.smo.chess.core

data class Castling(
    val isWhiteKingCastlePossible: Boolean = true,
    val isWhiteQueenCastlePossible: Boolean = true,
    val isBlackKingCastlePossible: Boolean = true,
    val isBlackQueenCastlePossible: Boolean = true,
)