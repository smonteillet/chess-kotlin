package fr.smo.chess.core

import fr.smo.chess.core.utils.OutcomeError

open class MoveCommandError(override val message: String) : OutcomeError

data class CannotLeaveYourOwnKingInCheck(val history: History) : MoveCommandError(
    message = "Having its own king checked after performing own move is illegal. " +
            "At move : ${history.halfMoveClock} ${history.moves.last()}"
)

data class PieceNotFound(val square: Square) : MoveCommandError(
    message = "Illegal Move because there is no piece at $square"
)

data class IllegalMove(val origin: Square, val destination: Square) : MoveCommandError(
    message = "There is no legal move from $origin to $destination"
)
