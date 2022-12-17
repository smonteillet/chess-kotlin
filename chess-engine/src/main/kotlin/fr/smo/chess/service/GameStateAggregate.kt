package fr.smo.chess.service

import fr.smo.chess.model.*
import fr.smo.chess.model.Color.BLACK
import fr.smo.chess.model.Color.WHITE
import fr.smo.chess.model.Piece.*
import fr.smo.chess.model.PieceType.KING
import fr.smo.chess.model.PieceType.PAWN
import fr.smo.chess.model.Square.*
import fr.smo.chess.model.Rank.*

class GameStateAggregate(
    private val pseudoLegalMovesFinder : PseudoLegalMovesFinder = PseudoLegalMovesFinder()
) {


    fun applyMoveRequest(moveRequest : MoveRequest, currentGameState: GameState) : GameState {
        val newGameState = buildNewGameState(moveRequest, currentGameState)
        checkIfCurrentColorKingIsCheckedAfterCurrentColorMove(moveRequest, newGameState)
        return updateGameOutcome(newGameState)
    }

    private fun checkIfCurrentColorKingIsCheckedAfterCurrentColorMove(move: MoveRequest, newGameState: GameState) {
        if (isChecked(newGameState.sideToMove.opposite(), newGameState)) {
            throw IllegalArgumentException("Having its own king checked after performing own move is illegal. " +
                    "MoveRequest : $move, state: $newGameState")
        }
    }

    private fun buildNewGameState(moveRequest : MoveRequest, currentGameState: GameState) : GameState {
        val fromPosition = getFromPosition(currentGameState, moveRequest)
        val move : Move = extractContextualizedMove(fromPosition, currentGameState, moveRequest)
        val newBoardPositions = applyRookMovesForCastling(move,
            applyPromotions(move,
                applyMoveOnBoard(move, currentGameState)
            )
        )
        return GameState(
            chessboard = Chessboard(piecesOnBoard = newBoardPositions),
            moveHistory = currentGameState.moveHistory.plus(move),
            sideToMove = currentGameState.sideToMove.opposite(),
            isBlackKingCastlePossible = isBlackKingSideCastlingStillPossible(move, currentGameState),
            isBlackQueenCastlePossible = isBlackQueenSideCastlingStillPossible(move, currentGameState),
            isWhiteQueenCastlePossible = isWhiteQueenSideCastlingStillPossible(move, currentGameState),
            isWhiteKingCastlePossible = isWhiteKingSideCastlingStillPossible(move, currentGameState),
            enPassantTargetSquare = getEnPassantTargetSquare(move),
            fullMoveCounter = getNewFullMoveCounter(move, currentGameState),
            halfMoveClock = getHalfMoveClock(move, currentGameState),
        )
    }

    private fun isChecked(kingColorThatMayBeChecked: Color, gameState: GameState) : Boolean {
        return pseudoLegalMovesFinder.getAllPseudoLegalMovesForPlayer(kingColorThatMayBeChecked.opposite(), gameState)
            .count { it.capturedPiece?.type == KING } > 0
    }

    private fun isStaleMate(colorThatMayHaveNoMove : Color, gameState: GameState) : Boolean {
        return pseudoLegalMovesFinder.getAllPseudoLegalMovesForPlayer(colorThatMayHaveNoMove, gameState.withoutCastleAndEnPassant())
                .map { MoveRequest(it.from, it.destination, it.promotedTo?.type) }
                .map { buildNewGameState(it, gameState.withoutCastleAndEnPassant()) }
                .all { isChecked(colorThatMayHaveNoMove, it) }
    }

    private fun isCheckMate(kingColorThatMayBeCheckMate : Color, gameState: GameState) : Boolean {
        if (isChecked(kingColorThatMayBeCheckMate, gameState)) {
            return pseudoLegalMovesFinder.getAllPseudoLegalMovesForPlayer(kingColorThatMayBeCheckMate,gameState)
                .map { MoveRequest(it.from, it.destination, it.promotedTo?.type) }
                .map { buildNewGameState(it, gameState.withoutCastleAndEnPassant()) }
                .all { isChecked(kingColorThatMayBeCheckMate, it) }
        }
        return false
    }

    private fun getHalfMoveClock(move: Move, gameState: GameState): Int {
        return if (move.capturedPiece != null || move.piece.type == PAWN) {
            0
        }
        else {
            gameState.halfMoveClock + 1
        }
    }

    private fun getNewFullMoveCounter(move : Move, gameState : GameState) : Int {
        return if (move.piece.color == BLACK) {
            gameState.fullMoveCounter + 1
        }
        else {
            gameState.fullMoveCounter
        }
    }

    private fun getEnPassantTargetSquare(move: Move): Square? {
        return if (move.piece == WHITE_PAWN && move.from.rank == SECOND && move.destination.rank == FOURTH) {
            move.from.withRank(THIRD)
        }
        else if (move.piece == BLACK_PAWN && move.from.rank == SEVENTH && move.destination.rank == FIFTH) {
            move.from.withRank(SIXTH)
        }
        else
            null
    }

    private fun is3Repetitions(gameState: GameState) : Boolean {
        val moveCount = gameState.moveHistory.size
       return gameState.moveHistory.size >= 12 &&
            gameState.moveHistory[moveCount - 1] == gameState.moveHistory[moveCount - 5] &&
            gameState.moveHistory[moveCount - 1] == gameState.moveHistory[moveCount - 9] &&
            gameState.moveHistory[moveCount - 2] == gameState.moveHistory[moveCount - 6] &&
            gameState.moveHistory[moveCount - 2] == gameState.moveHistory[moveCount - 10] &&
            gameState.moveHistory[moveCount - 3] == gameState.moveHistory[moveCount - 7] &&
            gameState.moveHistory[moveCount - 3] == gameState.moveHistory[moveCount - 11] &&
            gameState.moveHistory[moveCount - 4] == gameState.moveHistory[moveCount - 8] &&
            gameState.moveHistory[moveCount - 4] == gameState.moveHistory[moveCount - 12]

    }

    private fun updateGameOutcome(gameState : GameState) : GameState {
        return if (isCheckMate(gameState.sideToMove, gameState)) {
            val outcome = if (gameState.sideToMove == WHITE) {
                GameState.GameOutcome.BLACK_WIN
            } else {
                GameState.GameOutcome.WHITE_WIN
            }
            gameState.copy(gameOutcome = outcome)
        }
        else if (gameState.halfMoveClock == 50) {
            gameState.copy(gameOutcome = GameState.GameOutcome.DRAW)
        }
        else if(is3Repetitions(gameState)) {
            gameState.copy(gameOutcome = GameState.GameOutcome.DRAW)
        }
        else if (isStaleMate(gameState.sideToMove, gameState)) {
            gameState.copy(gameOutcome = GameState.GameOutcome.DRAW)
        }
        else
            gameState
    }

    private fun extractContextualizedMove(
        fromPosition: Position,
        gameState: GameState,
        moveRequest: MoveRequest
    ): Move {
        return pseudoLegalMovesFinder.getAllPseudoLegalMoves(fromPosition, gameState)
            .firstOrNull { it.destination == moveRequest.destination && it.promotedTo?.type == moveRequest.promotedPiece }
            ?: throw IllegalStateException("Invalid move $moveRequest")
    }

    private fun getFromPosition(gameState: GameState, moveRequest: MoveRequest): Position {
        return gameState.chessboard.getPositionAt(moveRequest.from)
            ?: throw IllegalStateException("There is no piece at ${moveRequest.from}")
    }

    private fun isWhiteKingSideCastlingStillPossible(move: Move, gameState: GameState): Boolean {
        return gameState.isWhiteKingCastlePossible &&
                (
                    (
                        move.piece.color == BLACK &&
                        ((move.capturedPiece != null && move.destination != H1) || move.capturedPiece == null)
                    ) ||
                    (
                        move.piece.color == WHITE &&
                        move.piece != WHITE_KING &&
                        (move.piece != WHITE_ROOK || move.from != H1)
                    )
                )
    }

    private fun isWhiteQueenSideCastlingStillPossible(move: Move, gameState: GameState): Boolean {
        return gameState.isWhiteQueenCastlePossible &&
                (
                    (
                            move.piece.color == BLACK &&
                            ((move.capturedPiece != null && move.destination != A1) || move.capturedPiece == null)
                    ) ||
                    (
                        move.piece.color == WHITE &&
                        move.piece != WHITE_KING &&
                        (move.piece != WHITE_ROOK || move.from != A1)
                    )
                )
    }

    private fun isBlackKingSideCastlingStillPossible(move: Move, gameState: GameState): Boolean {
        return gameState.isBlackKingCastlePossible &&
                (
                    (
                        move.piece.color == WHITE &&
                       ((move.capturedPiece != null && move.destination != H8) || move.capturedPiece == null)
                    ) ||
                    (
                        move.piece.color == BLACK &&
                        move.piece != BLACK_KING &&
                        (move.piece != BLACK_ROOK || move.from != H8)
                    )
                )
    }

    private fun isBlackQueenSideCastlingStillPossible(move: Move, gameState: GameState): Boolean {
        return gameState.isBlackQueenCastlePossible &&
                (
                    (
                        move.piece.color == WHITE &&
                        ((move.capturedPiece != null && move.destination != A8) || move.capturedPiece == null)
                    ) ||
                    (
                        move.piece.color == BLACK &&
                        move.piece != BLACK_KING &&
                        (move.piece != BLACK_ROOK || move.from != A8)
                    )
                )
    }

    private fun applyMoveOnBoard(move : Move, gameState: GameState): List<Position> {
        val opponentPawnThatHasBeenEnPassant =
            if (move.destination == gameState.enPassantTargetSquare && move.piece.type == PAWN) {
                if (move.piece.color == WHITE) {
                    gameState.enPassantTargetSquare.down()!!
                }
                else {
                    gameState.enPassantTargetSquare.up()!!
                }
            }
            else {
                null
            }
        return gameState.chessboard.piecesOnBoard
            .filter { it.square != move.from }
            .filter { it.square != move.destination }
            .filter { it.square != opponentPawnThatHasBeenEnPassant }
            .plus(Position(move.destination, move.piece))
    }

    private fun applyPromotions(move : Move, positions : List<Position>): List<Position> {
        if (move.piece.type == PAWN && move.promotedTo != null) {
            return positions
                .filter { it.square != move.destination }
                .plus(Position(move.destination, move.promotedTo))
        }
        return positions
    }

    private fun applyRookMovesForCastling(move: Move, positions: List<Position>): List<Position> {
        if (move.isKingCastle && move.piece.color == WHITE) {
            return positions
                .filter { it.square != H1 }
                .plus(Position(F1, WHITE_ROOK))
        } else if (move.isQueenCastle && move.piece.color == WHITE) {
            return positions
                .filter { it.square != A1 }
                .plus(Position(D1, WHITE_ROOK))
        } else if (move.isKingCastle && move.piece.color == BLACK) {
            return positions
                .filter { it.square != H8 }
                .plus(Position(F8, BLACK_ROOK))
        } else if (move.isQueenCastle && move.piece.color == BLACK) {
            return positions
                .filter { it.square != A8 }
                .plus(Position(D8, BLACK_ROOK))
        } else {
            return positions
        }
    }
}