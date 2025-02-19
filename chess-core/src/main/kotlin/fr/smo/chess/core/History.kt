package fr.smo.chess.core

data class History(
    val moves: List<Move> = listOf(),
    val fullMoveCounter: Int = 1,
    val halfMoveClock: Int = 0,
) {

    val lastMove: Move?
        get() = moves.lastOrNull()

    val lastMovedColor: Color?
        get() = lastMove?.piece?.color

    val isFiftyMoveRules: Boolean
        get() = halfMoveClock == 50

    val isThreeFoldRepetition: Boolean
        get() {
            val moveCount = moves.size
            return moves.size >= 12 &&
                    moves[moveCount - 1] == moves[moveCount - 5] &&
                    moves[moveCount - 1] == moves[moveCount - 9] &&
                    moves[moveCount - 2] == moves[moveCount - 6] &&
                    moves[moveCount - 2] == moves[moveCount - 10] &&
                    moves[moveCount - 3] == moves[moveCount - 7] &&
                    moves[moveCount - 3] == moves[moveCount - 11] &&
                    moves[moveCount - 4] == moves[moveCount - 8] &&
                    moves[moveCount - 4] == moves[moveCount - 12]
        }

    fun addMove(move: Move): History {
        return this.copy(
            moves = moves.plus(move),
            halfMoveClock = getHalfMoveClock(move),
            fullMoveCounter = getNewFullMoveCounter(move),
        )
    }

    fun markLastMoveAsChecked(): History {
        return moves.last().copy(isCheck = true).let { checkedLastMove ->
            this.copy(
                moves = moves.dropLast(1).plus(checkedLastMove)
            )
        }
    }

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