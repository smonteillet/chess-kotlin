package fr.smo.chess.core

enum class Status {
    CREATED,
    STARTED,
    BLACK_WIN,
    WHITE_WIN,
    DRAW,
    UNKNOWN_RESULT;

    val gameIsOver: Boolean
        get() = when (this) {
            BLACK_WIN, WHITE_WIN, DRAW, UNKNOWN_RESULT -> true
            else -> false
        }
}