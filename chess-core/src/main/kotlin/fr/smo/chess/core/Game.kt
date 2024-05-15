package fr.smo.chess.core

import fr.smo.chess.core.Color.WHITE
import fr.smo.chess.core.Rank.*
import fr.smo.chess.core.utils.*

open class MoveCommandError(override val message: String) : OutcomeError

data class IllegalMoveBecauseOwnKingInCheckError(val history: History) : MoveCommandError(
        message = "Having its own king checked after performing own move is illegal. " +
        "At move : ${history.halfMoveClock} ${history.moves.last()}"
)

data class IllegalMoveBecauseNoPieceAtStart(val square: Square) : MoveCommandError(
        message = "Illegal Move because there is no piece at $square"
)

data class IllegalMove(val origin: Square, val destination: Square) : MoveCommandError(
        message = "There is no legal move from $origin to $destination"
)

data class Game(
        val chessboard: Chessboard,
        val history: History,
        val sideToMove: Color = WHITE,
        val castling: Castling,
        val enPassantTargetSquare: Square? = null,
        val status: Status = Status.CREATED,
) {
    fun applyMove(
            moveCommand: MoveCommand,
            updateGameOutcomeState: Boolean = true,
            markMoveAsCheckedInHistory: Boolean = true,
    ): Outcome<Game, MoveCommandError> {
        return buildMove(moveCommand).flatMap { move ->
            isGameStateLegal(computeNewGameAfterMakeMove(move))
                    .map { newGameState -> markMoveAsCheckedInHistory(newGameState, move, markMoveAsCheckedInHistory) }
                    .map { updatedGame -> updateGameOutcome(updateGameOutcomeState, updatedGame) }
        }
    }

    private fun isGameStateLegal(newGameState: Game): Outcome<Game, MoveCommandError> {
        return if (isChecked(newGameState.sideToMove.opposite(), newGameState)) {
            Failure(IllegalMoveBecauseOwnKingInCheckError(newGameState.history))
        } else {
            Success(newGameState)
        }
    }

    private fun updateGameOutcome(updateGameState: Boolean, updatedGame: Game) = updateGameState.ifTrue {
        updatedGame.copy(status = getOutcome(updatedGame))
    } ?: updatedGame

    private fun markMoveAsCheckedInHistory(newGameState: Game, move: Move, markMoveAsCheckedInHistory: Boolean) : Game {
        return markMoveAsCheckedInHistory.ifTrue {
            isChecked(newGameState.sideToMove, newGameState).ifTrue {
                newGameState.copy(history = history.addMove(move.copy(isCheck = true)))
            } ?: newGameState
        } ?: newGameState
    }

    private fun computeNewGameAfterMakeMove(move: Move) =
            Game(
                    chessboard = chessboard.applyMoveOnBoard(move, enPassantTargetSquare)
                            .applyPromotions(move)
                            .applyRookMovesForCastling(move),
                    history = history.addMove(move),
                    sideToMove = sideToMove.opposite(),
                    castling = castling.updateCastlingAfterMove(move),
                    enPassantTargetSquare = getEnPassantTargetSquare(move),
            )


    fun applyMoves(vararg moveCommands: MoveCommand): Outcome<Game, MoveCommandError> {
        return moveCommands.fold(Success(this) as Outcome<Game, MoveCommandError>) { result, moveRequest ->
            result.flatMap { it.applyMove(moveRequest) }
        }
    }

    private fun getEnPassantTargetSquare(move: Move): Square? {
        return if (move.piece == Piece.WHITE_PAWN && move.origin.rank == RANK_2 && move.destination.rank == RANK_4) {
            move.origin.withRank(RANK_3)
        } else if (move.piece == Piece.BLACK_PAWN && move.origin.rank == RANK_7 && move.destination.rank == RANK_5) {
            move.origin.withRank(RANK_6)
        } else
            null
    }

    private fun buildMove(moveCommand: MoveCommand): Outcome<Move, MoveCommandError> =
            getOriginPiecePosition(moveCommand).flatMap { piecePosition ->
                getPseudoLegalMoves(this, piecePosition)
                        .firstOrNull { it.destination == moveCommand.destination && it.promotedTo?.type == moveCommand.promotedPiece }
                        ?.let { Success(it) }
                        ?: Failure(IllegalMove(moveCommand.origin,moveCommand.destination))
            }

    private fun getOriginPiecePosition(moveCommand: MoveCommand): Outcome<PiecePosition, MoveCommandError> {
        return chessboard.getPieceAt(moveCommand.origin)?.let { Success(PiecePosition(square = moveCommand.origin, piece = it)) }
                ?: Failure(IllegalMoveBecauseNoPieceAtStart(moveCommand.origin))
    }
}
