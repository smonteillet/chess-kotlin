package fr.smo.chess.core

import fr.smo.chess.core.Color.BLACK
import fr.smo.chess.core.Color.WHITE
import fr.smo.chess.core.PieceType.*


enum class Piece(
    val type: PieceType, val color: Color
) {

    WHITE_KING(KING, WHITE),
    WHITE_QUEEN(QUEEN, WHITE),
    WHITE_ROOK(ROOK, WHITE),
    WHITE_BISHOP(BISHOP, WHITE),
    WHITE_KNIGHT(KNIGHT, WHITE),
    WHITE_PAWN(PAWN, WHITE),

    BLACK_KING(KING, BLACK),
    BLACK_QUEEN(QUEEN, BLACK),
    BLACK_ROOK(ROOK, BLACK),
    BLACK_BISHOP(BISHOP, BLACK),
    BLACK_KNIGHT(KNIGHT, BLACK),
    BLACK_PAWN(PAWN, BLACK);


}
