package fr.smo.chess.core

import fr.smo.chess.core.Color.BLACK
import fr.smo.chess.core.Color.WHITE
import fr.smo.chess.core.Piece.*
import fr.smo.chess.core.PieceType.*
import fr.smo.chess.core.Square.*

/**
 * In Pseudo-legal move generation pieces obey their normal rules of movement, but they're not checked beforehand to see
 * if they'll leave the king in check. It is left up to the move-making function to test the move, or it is even possible
 * to let the king remain in check and only test for the capture of the king on the next move
 */
class PseudoLegalMovesFinder {

    fun getAllPseudoLegalMoves(fromPosition: Position, game: Game): List<Move> {
        return when (fromPosition.piece.type) {
            PAWN -> getPawnPseudoLegalMoves(fromPosition, game)
            BISHOP -> getBishopPseudoLegalMoves(fromPosition, game.chessboard)
            KNIGHT -> getKnightPseudoLegalMoves(fromPosition, game.chessboard)
            QUEEN -> getQueenPseudoLegalMoves(fromPosition, game.chessboard)
            KING -> getKingPseudoLegalMoves(fromPosition, game)
            ROOK -> getRookPseudoLegalMoves(fromPosition, game.chessboard)
        }
    }

    fun getAllPseudoLegalMovesForPlayer(color: Color, game: Game): List<Move> {
        return game.chessboard.piecesOnBoard
            .filter { it.piece.color == color }
            .flatMap {
                getAllPseudoLegalMoves(it, game)
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


    private fun getPawnPseudoLegalMoves(pawnPosition: Position, game: Game): List<Move> =
        (if (pawnPosition.piece.color == WHITE) {
            getPawnOneSquareAndTwoSquaresMoves(
                chessboard = game.chessboard,
                pawnPosition = pawnPosition,
                hasPawnNotAlreadyMoved = pawnPosition.square.rank == Rank.RANK_2,
                pawnFrontMoveFunction = { it.up() },
            ).plus(
                getPawnDiagonalMoves(game.chessboard, pawnPosition) { it.upRight() }
            ).plus(
                getPawnDiagonalMoves(game.chessboard, pawnPosition) { it.upLeft() }
            )
        } else {
            getPawnOneSquareAndTwoSquaresMoves(
                chessboard = game.chessboard,
                pawnPosition = pawnPosition,
                hasPawnNotAlreadyMoved = pawnPosition.square.rank == Rank.RANK_7,
                pawnFrontMoveFunction = { it.down() },
            ).plus(
                getPawnDiagonalMoves(game.chessboard, pawnPosition) { it.downRight() }
            ).plus(
                getPawnDiagonalMoves(game.chessboard, pawnPosition) { it.downLeft() }
            )
        }.plus(
            getPawnEnPassantMove(pawnPosition, game)
        )
                ).filterNotNull()


    private fun getPawnEnPassantMove(pawnPosition: Position, game: Game): Move? {
        if (game.enPassantTargetSquare != null) {
            if (
                pawnPosition.piece.color == WHITE &&
                (
                        game.enPassantTargetSquare == pawnPosition.square.up()?.right() ||
                                game.enPassantTargetSquare == pawnPosition.square.up()?.left()
                        )
            ) {
                return Move(
                    piece = pawnPosition.piece,
                    capturedPiece = BLACK_PAWN,
                    from = pawnPosition.square,
                    destination = game.enPassantTargetSquare,
                )
            } else if (
                pawnPosition.piece.color == BLACK &&
                (
                        game.enPassantTargetSquare == pawnPosition.square.down()?.right() ||
                                game.enPassantTargetSquare == pawnPosition.square.down()?.left()
                        )
            ) {
                return Move(
                    piece = pawnPosition.piece,
                    capturedPiece = WHITE_PAWN,
                    from = pawnPosition.square,
                    destination = game.enPassantTargetSquare,
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
        return if (color == WHITE && position.rank == Rank.RANK_8) {
            listOf(WHITE_QUEEN, WHITE_ROOK, WHITE_BISHOP, WHITE_KNIGHT)
        } else if (color == BLACK && position.rank == Rank.RANK_1) {
            listOf(BLACK_QUEEN, BLACK_ROOK, BLACK_BISHOP, BLACK_KNIGHT)
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
        return getLegalMovesFollowingDirection(fromPosition = bishopPosition, chessboard = chessboard) { it.upLeft() } +
                getLegalMovesFollowingDirection(fromPosition = bishopPosition, chessboard = chessboard) { it.upRight() } +
                getLegalMovesFollowingDirection(fromPosition = bishopPosition, chessboard = chessboard) { it.downLeft() } +
                getLegalMovesFollowingDirection(fromPosition = bishopPosition, chessboard = chessboard) { it.downRight() }
    }

    private fun getKingPseudoLegalMoves(kingPosition: Position, game: Game): List<Move> {
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
            .filterNot { game.chessboard.isPiecePresentAtAndHasColor(it, kingPosition.piece.color) }
            .map {
                Move(
                    piece = kingPosition.piece,
                    from = kingPosition.square,
                    destination = it,
                    capturedPiece = game.chessboard.getPositionAt(it)?.piece
                )
            }.plus(getCastleMoveIfPossible(kingPosition, game))
    }

    private fun getCastleMoves(
        game: Game,
        kingPosition: Position,
        isKingCastlePossible: Boolean,
        isQueenCastlePossible: Boolean,
        kingDestinationKingCastle: Square,
        kingDestinationQueenCastle: Square,
        kingCastlingPathSquares: List<Square>,
        queenCastlingPathSquares: List<Square>,
    ): List<Move> {
        val isTherePiecesOnQueenCastlingPath = game.chessboard.piecesOnBoard
            .any { queenCastlingPathSquares.minus(kingPosition.square).contains(it.square) }
        val isTherePiecesOnKingCastlingPath = game.chessboard.piecesOnBoard
            .any { kingCastlingPathSquares.minus(kingPosition.square).contains(it.square) }
        //FIXME feels weird now
        val gameWithoutCastling = game.copy(
            isWhiteKingCastlePossible = false, isWhiteQueenCastlePossible = false, isBlackQueenCastlePossible = false,
            isBlackKingCastlePossible = false, enPassantTargetSquare = null,
        )
        val allPseudoLegalMovesForPlayer = getAllPseudoLegalMovesForPlayer(kingPosition.piece.color.opposite(), gameWithoutCastling)
        val hasKingCastleSquaresUnderAttack: Boolean = allPseudoLegalMovesForPlayer.any { kingCastlingPathSquares.contains(it.destination) }
        val hasQueenCastleSquaresUnderAttack: Boolean = allPseudoLegalMovesForPlayer.any { queenCastlingPathSquares.contains(it.destination) }

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

    private fun getCastleMoveIfPossible(kingPosition: Position, game: Game): List<Move> {
        return if (game.isNextPlayCouldBeWhiteCastle) {
            getCastleMoves(
                game = game,
                kingPosition = kingPosition,
                isKingCastlePossible = game.isWhiteKingCastlePossible,
                isQueenCastlePossible = game.isWhiteQueenCastlePossible,
                kingDestinationKingCastle = G1,
                kingDestinationQueenCastle = C1,
                kingCastlingPathSquares = listOf(E1, F1, G1),
                queenCastlingPathSquares = listOf(E1, D1, C1),
            )
        } else if (game.isNextPlayCouldBeBlackCastle) {
            getCastleMoves(
                game = game,
                kingPosition = kingPosition,
                isKingCastlePossible = game.isBlackKingCastlePossible,
                isQueenCastlePossible = game.isBlackQueenCastlePossible,
                kingDestinationKingCastle = G8,
                kingDestinationQueenCastle = C8,
                kingCastlingPathSquares = listOf(E8, F8, G8),
                queenCastlingPathSquares = listOf(E8, D8, C8),
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