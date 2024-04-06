package fr.smo.chess.core

import fr.smo.chess.core.utils.Failure
import fr.smo.chess.core.utils.Success
import fr.smo.chess.core.utils.ifTrue

// TODO this could be integrated back into the Game class
//see https://www.chess.com/article/view/how-chess-games-can-end-8-ways-explained

fun getOutcome(game: Game) : Status {
    return if (isDraw(game)) {
        Status.DRAW
    }
    else if (isCheckMate(game)) {
       if (game.history.lastMoveColor() == Color.WHITE) {
            Status.WHITE_WIN
        } else {
            Status.BLACK_WIN
        }
    }
    else if (isStaleMate(game)) {
        Status.DRAW
    } else {
        Status.STARTED
    }
}

private fun isCheckMate(game : Game): Boolean {
    return game.history.moves.last().isCheck.ifTrue {
        hasNoLegalMove(game.sideToMove, game)
    } ?: false
}

fun isChecked(kingColorThatMayBeChecked: Color, game: Game): Boolean {
    return game.chessboard.hasAPseudoLegalMovesSatisfying(kingColorThatMayBeChecked.opposite(), game) { move ->
        move.capturedPiece?.type == PieceType.KING
    }
}

private fun isDraw(game: Game) : Boolean {
    // TODO need to implement insufficient material
    // https://www.chess.com/article/view/how-chess-games-can-end-8-ways-explained#insufficient-material
    return isFiftyMovesRule(game) || isThreefoldRepetitions(game)
}

private fun isThreefoldRepetitions(game: Game): Boolean {
    val moves = game.history.moves
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

fun isFiftyMovesRule(game: Game): Boolean = game.history.halfMoveClock == 50

private fun isStaleMate(game: Game): Boolean {
    return hasNoLegalMove(game.sideToMove, game)
}

private fun hasNoLegalMove(color: Color, game: Game) : Boolean {
    return (
        game.chessboard.hasAPseudoLegalMovesSatisfying(color, game) { move ->
            val moveCommand = MoveCommand(move.origin, move.destination, move.promotedTo?.type)
            when (game.applyMove(moveCommand, false)) {
                is Success -> true
                is Failure -> false
            }
        }.not()
    )
}