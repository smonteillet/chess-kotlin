package fr.smo.chess.core.algs

import fr.smo.chess.core.*
import fr.smo.chess.core.utils.Failure
import fr.smo.chess.core.utils.Success
import fr.smo.chess.core.utils.ifTrue

// TODO this could be integrated back into the Game class
//see https://www.chess.com/article/view/how-chess-games-can-end-8-ways-explained

fun getOutcome(position: Position) : Status {
    return if (isDraw(position)) {
        Status.DRAW
    }
    else if (isCheckMate(position)) {
       if (position.history.lastMoveColor() == Color.WHITE) {
           Status.WHITE_WIN
        } else {
           Status.BLACK_WIN
        }
    }
    else if (isStaleMate(position)) {
        Status.DRAW
    } else {
        Status.STARTED
    }
}

private fun isCheckMate(position : Position): Boolean {
    return position.history.moves.last().isCheck.ifTrue {
        hasNoLegalMove(position.sideToMove, position)
    } ?: false
}

fun isChecked(kingColorThatMayBeChecked: Color, position: Position): Boolean {
    return hasAPseudoLegalMovesSatisfying(kingColorThatMayBeChecked.opposite(), position) { move ->
        move.capturedPiece?.type == PieceType.KING
    }
}

private fun isDraw(position: Position) : Boolean {
    // TODO need to implement insufficient material
    // https://www.chess.com/article/view/how-chess-games-can-end-8-ways-explained#insufficient-material
    return isFiftyMovesRule(position) || isThreefoldRepetitions(position)
}

private fun isThreefoldRepetitions(position: Position): Boolean {
    val moves = position.history.moves
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

fun isFiftyMovesRule(position: Position): Boolean = position.history.halfMoveClock == 50

private fun isStaleMate(position: Position): Boolean {
    return hasNoLegalMove(position.sideToMove, position)
}

private fun hasNoLegalMove(color: Color, position: Position) : Boolean {
    return (
        hasAPseudoLegalMovesSatisfying(color, position) { move ->
            val moveCommand = MoveCommand(move.origin, move.destination, move.promotedTo?.type)
            when (position.isLegalMove(moveCommand)) {
                is Success -> true
                is Failure -> false
            }
        }.not()
    )
}