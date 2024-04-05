package fr.smo.chess.core

data class History(
    val moves: List<Move> = listOf(),
    val fullMoveCounter: Int = 1,
    val halfMoveClock: Int = 0,
) {

    fun addMove(move: Move): History {
        return this.copy(
            moves = moves.plus(move),
            halfMoveClock = getHalfMoveClock(move),
            fullMoveCounter = getNewFullMoveCounter(move),
        )
    }

    fun lastMoveColor() : Color = moves.last().piece.color

    private fun getHalfMoveClock(move: Move): Int {
        return if (move.capturedPiece != null || move.piece.type == PieceType.PAWN) {
            0
        } else {
            halfMoveClock + 1
        }
    }

    private fun getNewFullMoveCounter(move: Move): Int {
        return if (move.piece.color == Color.BLACK) {
            fullMoveCounter + 1
        } else {
            fullMoveCounter
        }
    }

}