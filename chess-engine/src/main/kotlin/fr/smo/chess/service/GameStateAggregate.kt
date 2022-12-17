package fr.smo.chess.service

import fr.smo.chess.model.*
import fr.smo.chess.model.Color.BLACK
import fr.smo.chess.model.Color.WHITE
import fr.smo.chess.model.Piece.Companion.blackKing
import fr.smo.chess.model.Piece.Companion.blackPawn
import fr.smo.chess.model.Piece.Companion.blackRook
import fr.smo.chess.model.Piece.Companion.whiteKing
import fr.smo.chess.model.Piece.Companion.whitePawn
import fr.smo.chess.model.Piece.Companion.whiteRook
import fr.smo.chess.model.Piece.Type.KING
import fr.smo.chess.model.Piece.Type.PAWN
import fr.smo.chess.model.Square.Companion.square

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
        var newBoardPositions = applyMoveOnBoard(move, currentGameState)
        newBoardPositions = applyPromotions(move, newBoardPositions)
        newBoardPositions = applyRookMovesForCastling(move, newBoardPositions)
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
        return if (move.piece == whitePawn() && move.from.row == 2 && move.destination.row == 4) {
            move.from.withRow(3)
        }
        else if (move.piece == blackPawn() && move.from.row == 7 && move.destination.row == 5) {
            move.from.withRow(6)
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
                        ((move.capturedPiece != null && move.destination != square("h1")) || move.capturedPiece == null)
                    ) ||
                    (
                        move.piece.color == WHITE &&
                        move.piece != whiteKing() &&
                        (move.piece != whiteRook() || move.from != square("h1"))
                    )
                )
    }

    private fun isWhiteQueenSideCastlingStillPossible(move: Move, gameState: GameState): Boolean {
        return gameState.isWhiteQueenCastlePossible &&
                (
                    (
                            move.piece.color == BLACK &&
                            ((move.capturedPiece != null && move.destination != square("a1")) || move.capturedPiece == null)
                    ) ||
                    (
                        move.piece.color == WHITE &&
                        move.piece != whiteKing() &&
                        (move.piece != whiteRook() || move.from != square("a1"))
                    )
                )
    }

    private fun isBlackKingSideCastlingStillPossible(move: Move, gameState: GameState): Boolean {
        return gameState.isBlackKingCastlePossible &&
                (
                    (
                        move.piece.color == WHITE &&
                       ((move.capturedPiece != null && move.destination != square("h8")) || move.capturedPiece == null)
                    ) ||
                    (
                        move.piece.color == BLACK &&
                        move.piece != blackKing() &&
                        (move.piece != blackRook() || move.from != square("h8"))
                    )
                )
    }

    private fun isBlackQueenSideCastlingStillPossible(move: Move, gameState: GameState): Boolean {
        return gameState.isBlackQueenCastlePossible &&
                (
                    (
                        move.piece.color == WHITE &&
                        ((move.capturedPiece != null && move.destination != square("a8")) || move.capturedPiece == null)
                    ) ||
                    (
                        move.piece.color == BLACK &&
                        move.piece != blackKing() &&
                        (move.piece != blackRook() || move.from != square("a8"))
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
                .filter { it.square != square("h1") }
                .plus(Position(square("f1"), whiteRook()))
        } else if (move.isQueenCastle && move.piece.color == WHITE) {
            return positions
                .filter { it.square != square("a1") }
                .plus(Position(square("d1"), whiteRook()))
        } else if (move.isKingCastle && move.piece.color == BLACK) {
            return positions
                .filter { it.square != square("h8") }
                .plus(Position(square("f8"), blackRook()))
        } else if (move.isQueenCastle && move.piece.color == BLACK) {
            return positions
                .filter { it.square != square("a8") }
                .plus(Position(square("d8"), blackRook()))
        } else {
            return positions
        }
    }
}