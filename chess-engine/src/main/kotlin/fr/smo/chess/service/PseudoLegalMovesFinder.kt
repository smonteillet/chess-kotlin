package fr.smo.chess.service

import fr.smo.chess.model.*
import fr.smo.chess.model.Color.BLACK
import fr.smo.chess.model.Color.WHITE
import fr.smo.chess.model.Piece.Companion.blackPawn
import fr.smo.chess.model.Piece.Companion.piece
import fr.smo.chess.model.Piece.Companion.whitePawn
import fr.smo.chess.model.Piece.Type.*
import fr.smo.chess.model.Square.Companion.square

/**
 * In Pseudo-legal move generation pieces obey their normal rules of movement, but they're not checked beforehand to see
 * if they'll leave the king in check. It is left up to the move-making function to test the move, or it is even possible
 * to let the king remain in check and only test for the capture of the king on the next move
 */
class PseudoLegalMovesFinder {

    fun getAllPseudoLegalMoves(fromPosition: Position, gameState: GameState): List<Move> {
        return when (fromPosition.piece.type) {
            PAWN -> getPawnPseudoLegalMoves(fromPosition, gameState)
            BISHOP -> getBishopPseudoLegalMoves(fromPosition, gameState.chessboard)
            KNIGHT -> getKnightPseudoLegalMoves(fromPosition, gameState.chessboard)
            QUEEN -> getQueenPseudoLegalMoves(fromPosition, gameState.chessboard)
            KING -> getKingPseudoLegalMoves(fromPosition, gameState)
            ROOK -> getRookPseudoLegalMoves(fromPosition, gameState.chessboard)
        }
    }

    fun getAllPseudoLegalMovesForPlayer(color: Color, gameState: GameState): List<Move> {
        return gameState.chessboard.piecesOnBoard
            .filter { it.piece.color == color }
            .flatMap {
                getAllPseudoLegalMoves(it, gameState)
            }
    }

    private fun getKnightPseudoLegalMoves(knightPosition: Position, chessboard: Chessboard): List<Move> {
        return listOfNotNull(
            knightPosition.square.up()?.up()?.left(),
            knightPosition.square.up()?.up()?.right(),
            knightPosition.square.down()?.down()?.right(),
            knightPosition.square.down()?.down()?.left(),
            knightPosition.square.left()?.left()?.up(),
            knightPosition.square.left()?.left()?.down(),
            knightPosition.square.right()?.right()?.up(),
            knightPosition.square.right()?.right()?.down(),
        ).filterNot { chessboard.isPiecePresentAtAndHasColor(it, knightPosition.piece.color) }
            .map {
                Move(
                    piece = knightPosition.piece,
                    from = knightPosition.square,
                    destination = it,
                    capturedPiece = chessboard.getPositionAt(it)?.piece,
                )
            }
    }


    private fun getPawnPseudoLegalMoves(pawnPosition: Position, gameState: GameState): List<Move> =
        (if (pawnPosition.piece.color == WHITE) {
            getPawnOneSquareAndTwoSquaresMoves(
                chessboard = gameState.chessboard,
                pawnPosition = pawnPosition,
                hasPawnNotAlreadyMoved = pawnPosition.square.row == 2,
                pawnFrontMoveFunction = { it.up() },
            ).plus(
                getPawnDiagonalMoves(gameState.chessboard, pawnPosition) { it.upRight() }
            ).plus(
                getPawnDiagonalMoves(gameState.chessboard, pawnPosition) { it.upLeft() }
            )
        } else {
            getPawnOneSquareAndTwoSquaresMoves(
                chessboard = gameState.chessboard,
                pawnPosition = pawnPosition,
                hasPawnNotAlreadyMoved = pawnPosition.square.row == 7,
                pawnFrontMoveFunction = { it.down() },
            ).plus(
                getPawnDiagonalMoves(gameState.chessboard, pawnPosition) { it.downRight() }
            ).plus(
                getPawnDiagonalMoves(gameState.chessboard, pawnPosition) { it.downLeft() }
            )
        }.plus(
            getPawnEnPassantMove(pawnPosition, gameState)
        )
                ).filterNotNull()


    private fun getPawnEnPassantMove(pawnPosition: Position, gameState: GameState): Move? {
        if (gameState.enPassantTargetSquare != null) {
            if (
                pawnPosition.piece.color == WHITE &&
                (
                        gameState.enPassantTargetSquare == pawnPosition.square.up()?.right() ||
                                gameState.enPassantTargetSquare == pawnPosition.square.up()?.left()
                        )
            ) {
                return Move(
                    piece = pawnPosition.piece,
                    capturedPiece = blackPawn(),
                    from = pawnPosition.square,
                    destination = gameState.enPassantTargetSquare,
                )
            } else if (
                pawnPosition.piece.color == BLACK &&
                (
                        gameState.enPassantTargetSquare == pawnPosition.square.down()?.right() ||
                                gameState.enPassantTargetSquare == pawnPosition.square.down()?.left()
                        )
            ) {
                return Move(
                    piece = pawnPosition.piece,
                    capturedPiece = whitePawn(),
                    from = pawnPosition.square,
                    destination = gameState.enPassantTargetSquare,
                )
            }
        }
        return null
    }

    private fun getPawnOneSquareAndTwoSquaresMoves(
        chessboard: Chessboard,
        pawnPosition: Position,
        hasPawnNotAlreadyMoved: Boolean,
        pawnFrontMoveFunction: (Square) -> Square?
    ): List<Move> {
        return pawnFrontMoveFunction.invoke(pawnPosition.square)?.let { oneSquareMove ->
            if (chessboard.getPositionAt(oneSquareMove) == null) {
                getOneSquarePawnMoves(pawnPosition, oneSquareMove)
                    .plus(
                        getPawnTwoSquaresMove(hasPawnNotAlreadyMoved, pawnFrontMoveFunction, chessboard, pawnPosition)
                    ).filterNotNull()
            } else {
                emptyList()
            }
        } ?: emptyList()
    }

    private fun getPawnTwoSquaresMove(
        hasPawnNotAlreadyMoved: Boolean,
        pawnFrontMoveFunction: (Square) -> Square?,
        chessboard: Chessboard,
        pawnPosition: Position
    ): Move? {
        return if (hasPawnNotAlreadyMoved) {
            val twoSquaresMove = pawnFrontMoveFunction.invoke(
                pawnFrontMoveFunction.invoke(pawnPosition.square)!!
            )
            if (twoSquaresMove != null && chessboard.getPositionAt(twoSquaresMove) == null) {
                Move(
                    piece = pawnPosition.piece,
                    from = pawnPosition.square,
                    destination = twoSquaresMove,
                )
            } else {
                null
            }
        } else {
            null
        }
    }

    private fun getOneSquarePawnMoves(pawnPosition: Position, oneSquareMove: Square): List<Move> =
        getPromotedPieces(pawnPosition.piece.color, oneSquareMove).map {
            Move(
                piece = pawnPosition.piece,
                from = pawnPosition.square,
                destination = oneSquareMove,
                promotedTo = it
            )
        }.ifEmpty {
            listOf(
                Move(
                    piece = pawnPosition.piece,
                    from = pawnPosition.square,
                    destination = oneSquareMove,
                )
            )
        }


    private fun getPromotedPieces(color: Color, position: Square): List<Piece> {
        return if (color == WHITE && position.row == 8) {
            listOf(piece(QUEEN, WHITE), piece(ROOK, WHITE), piece(BISHOP, WHITE), piece(KNIGHT, WHITE))
        } else if (color == BLACK && position.row == 1) {
            listOf(piece(QUEEN, BLACK), piece(ROOK, BLACK), piece(BISHOP, BLACK), piece(KNIGHT, BLACK))
        } else {
            emptyList()
        }
    }

    private fun getPawnDiagonalMoves(
        chessboard: Chessboard,
        pawnPosition: Position,
        diagonalFunction: (Square) -> Square?
    ): Move? {
        val diagonalSquare = diagonalFunction.invoke(pawnPosition.square)
        if (diagonalSquare != null) {
            val pieceAtDiagonal = chessboard.getPositionAt(diagonalSquare)
            if (pieceAtDiagonal != null && pieceAtDiagonal.piece.color == pawnPosition.piece.color.opposite()) {
                return Move(
                    piece = pawnPosition.piece,
                    from = pawnPosition.square,
                    destination = diagonalSquare,
                    capturedPiece = pieceAtDiagonal.piece,
                )
            }
        }
        return null
    }

    private fun getQueenPseudoLegalMoves(queenPosition: Position, chessboard: Chessboard): List<Move> =
        getBishopPseudoLegalMoves(queenPosition, chessboard).plus(getRookPseudoLegalMoves(queenPosition, chessboard))

    private fun getRookPseudoLegalMoves(rookPosition: Position, chessboard: Chessboard): List<Move> {
        return getLegalMovesFollowingDirection(fromPosition = rookPosition, chessboard = chessboard) { it.up() } +
                getLegalMovesFollowingDirection(fromPosition = rookPosition, chessboard = chessboard) { it.down() } +
                getLegalMovesFollowingDirection(fromPosition = rookPosition, chessboard = chessboard) { it.left() } +
                getLegalMovesFollowingDirection(fromPosition = rookPosition, chessboard = chessboard) { it.right() }
    }

    private fun getBishopPseudoLegalMoves(bishopPosition: Position, chessboard: Chessboard): List<Move> {
        return getLegalMovesFollowingDirection(
            fromPosition = bishopPosition,
            chessboard = chessboard
        ) { it.upLeft() } +
                getLegalMovesFollowingDirection(
                    fromPosition = bishopPosition,
                    chessboard = chessboard
                ) { it.upRight() } +
                getLegalMovesFollowingDirection(
                    fromPosition = bishopPosition,
                    chessboard = chessboard
                ) { it.downLeft() } +
                getLegalMovesFollowingDirection(
                    fromPosition = bishopPosition,
                    chessboard = chessboard
                ) { it.downRight() }
    }

    private fun getKingPseudoLegalMoves(kingPosition: Position, gameState: GameState): List<Move> {
        return listOfNotNull(
            kingPosition.square.up(),
            kingPosition.square.down(),
            kingPosition.square.left(),
            kingPosition.square.right(),
            kingPosition.square.upLeft(),
            kingPosition.square.upRight(),
            kingPosition.square.downRight(),
            kingPosition.square.downLeft()
        )
            .filterNot { gameState.chessboard.isPiecePresentAtAndHasColor(it, kingPosition.piece.color) }
            .map {
                Move(
                    piece = kingPosition.piece,
                    from = kingPosition.square,
                    destination = it,
                    capturedPiece = gameState.chessboard.getPositionAt(it)?.piece
                )
            }.plus(getCastleMoveIfPossible(kingPosition, gameState))
    }

    private fun getCastleMoves(
        gameState: GameState,
        kingPosition: Position,
        isKingCastlePossible: Boolean,
        isQueenCastlePossible: Boolean,
        kingDestinationKingCastle: Square,
        kingDestinationQueenCastle: Square,
        kingCastlingPathSquares: List<Square>,
        queenCastlingPathSquares: List<Square>,
    ): List<Move> {
        val isTherePiecesOnQueenCastlingPath = gameState.chessboard.piecesOnBoard
            .any { queenCastlingPathSquares.minus(kingPosition.square).contains(it.square) }
        val isTherePiecesOnKingCastlingPath = gameState.chessboard.piecesOnBoard
            .any { kingCastlingPathSquares.minus(kingPosition.square).contains(it.square) }
        val gameStateWithoutCastling = gameState.copy(
            isWhiteKingCastlePossible = false, isWhiteQueenCastlePossible = false, isBlackQueenCastlePossible = false,
            isBlackKingCastlePossible = false, enPassantTargetSquare = null,
        )
        val hasKingCastleSquaresUnderAttack: Boolean =
            getAllPseudoLegalMovesForPlayer(kingPosition.piece.color.opposite(), gameStateWithoutCastling)
                .any { kingCastlingPathSquares.contains(it.destination) }
        val hasQueenCastleSquaresUnderAttack: Boolean =
            getAllPseudoLegalMovesForPlayer(kingPosition.piece.color.opposite(), gameStateWithoutCastling)
                .any { queenCastlingPathSquares.contains(it.destination) }

        return listOfNotNull(
            if (isKingCastlePossible && !isTherePiecesOnKingCastlingPath && !hasKingCastleSquaresUnderAttack) {
                Move(
                    piece = kingPosition.piece,
                    from = kingPosition.square,
                    destination = kingDestinationKingCastle,
                    isKingCastle = true,
                )
            } else null,
            if (isQueenCastlePossible && !isTherePiecesOnQueenCastlingPath && !hasQueenCastleSquaresUnderAttack) {
                Move(
                    piece = kingPosition.piece,
                    from = kingPosition.square,
                    destination = kingDestinationQueenCastle,
                    isQueenCastle = true,
                )
            } else null
        )
    }

    private fun getCastleMoveIfPossible(kingPosition: Position, gameState: GameState): List<Move> {
        return if (gameState.isNextPlayCouldBeWhiteCastle) {
            getCastleMoves(
                gameState = gameState,
                kingPosition = kingPosition,
                isKingCastlePossible = gameState.isWhiteKingCastlePossible,
                isQueenCastlePossible = gameState.isWhiteQueenCastlePossible,
                kingDestinationKingCastle = square("g1"),
                kingDestinationQueenCastle = square("c1"),
                kingCastlingPathSquares = listOf(square("e1"), square("f1"), square("g1")),
                queenCastlingPathSquares = listOf(square("e1"), square("d1"), square("c1")),
            )
        } else if (gameState.isNextPlayCouldBeBlackCastle) {
            getCastleMoves(
                gameState = gameState,
                kingPosition = kingPosition,
                isKingCastlePossible = gameState.isBlackKingCastlePossible,
                isQueenCastlePossible = gameState.isBlackQueenCastlePossible,
                kingDestinationKingCastle = square("g8"),
                kingDestinationQueenCastle = square("c8"),
                kingCastlingPathSquares = listOf(square("e8"), square("f8"), square("g8")),
                queenCastlingPathSquares = listOf(square("e8"), square("d8"), square("c8")),
            )
        } else {
            emptyList()
        }
    }

    private fun getLegalMovesFollowingDirection(
        fromPosition: Position,
        currentSquare: Square = fromPosition.square,
        chessboard: Chessboard,
        direction: (from: Square) -> Square?
    ): List<Move> {
        return direction.invoke(currentSquare).let { newSquare ->
            if (newSquare != null) {
                val newPosition = chessboard.getPositionAt(newSquare)
                if (newPosition == null) {
                    getLegalMovesFollowingDirection(fromPosition, newSquare, chessboard, direction) +
                            Move(
                                piece = fromPosition.piece,
                                from = fromPosition.square,
                                destination = newSquare,
                            )
                } else if (newPosition.hasNotSameColorPiece(fromPosition)) {
                    listOf(
                        Move(
                            piece = fromPosition.piece,
                            from = fromPosition.square,
                            destination = newSquare,
                            capturedPiece = newPosition.piece,
                        )
                    )
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }
    }
}