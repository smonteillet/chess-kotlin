package fr.smo.chess.core

import fr.smo.chess.core.Color.BLACK
import fr.smo.chess.core.Color.WHITE
import fr.smo.chess.core.Rank.*

data class Position(val square: Square, val piece: Piece) {

    fun hasNotSameColorPiece(other: Position): Boolean {
        return piece.color != other.piece.color
    }

    /**
     * In Pseudo-legal move generation pieces obey their normal rules of movement, but they're not checked beforehand to see
     * if they'll leave the king in check. It is left up to the move-making function to test the move, or it is even possible
     * to let the king remain in check and only test for the capture of the king on the next move
     */
    fun getAllPseudoLegalMoves(game: Game): List<Move> {
        return when (piece.type) {
            PieceType.PAWN -> getPawnPseudoLegalMoves(game)
            PieceType.BISHOP -> getBishopPseudoLegalMoves(game.chessboard)
            PieceType.KNIGHT -> getKnightPseudoLegalMoves(game.chessboard)
            PieceType.QUEEN -> getQueenPseudoLegalMoves(game.chessboard)
            PieceType.KING -> getKingPseudoLegalMoves(game)
            PieceType.ROOK -> getRookPseudoLegalMoves(game.chessboard)
        }
    }

    private fun getKnightPseudoLegalMoves(chessboard: Chessboard): List<Move> {
        return listOfNotNull(
            square.up()?.up()?.left(),
            square.up()?.up()?.right(),
            square.down()?.down()?.right(),
            square.down()?.down()?.left(),
            square.left()?.left()?.up(),
            square.left()?.left()?.down(),
            square.right()?.right()?.up(),
            square.right()?.right()?.down(),
        ).filterNot { chessboard.isPiecePresentAtAndHasColor(it, piece.color) }
            .map {
                Move(
                    piece = piece,
                    from = square,
                    destination = it,
                    capturedPiece = chessboard.getPositionAt(it)?.piece,
                )
            }
    }


    private fun getPawnPseudoLegalMoves(game: Game): List<Move> =
        (if (piece.color == WHITE) {
            getPawnOneSquareAndTwoSquaresMoves(
                chessboard = game.chessboard,
                pawnPosition = this,
                hasPawnNotAlreadyMoved = square.rank == RANK_2,
                pawnFrontMoveFunction = { it.up() },
            ).plus(
                getPawnDiagonalMoves(game.chessboard) { it.upRight() }
            ).plus(
                getPawnDiagonalMoves(game.chessboard) { it.upLeft() }
            )
        } else {
            getPawnOneSquareAndTwoSquaresMoves(
                chessboard = game.chessboard,
                pawnPosition = this,
                hasPawnNotAlreadyMoved = square.rank == RANK_7,
                pawnFrontMoveFunction = { it.down() },
            ).plus(
                getPawnDiagonalMoves(game.chessboard) { it.downRight() }
            ).plus(
                getPawnDiagonalMoves(game.chessboard) { it.downLeft() }
            )
        }.plus(
            getPawnEnPassantMove(game)
        )
                ).filterNotNull()


    private fun getPawnEnPassantMove(game: Game): Move? {
        if (game.enPassantTargetSquare != null) {
            if (
                piece.color == WHITE &&
                (
                        game.enPassantTargetSquare == square.up()?.right() ||
                                game.enPassantTargetSquare == square.up()?.left()
                        )
            ) {
                return Move(
                    piece = piece,
                    capturedPiece = Piece.BLACK_PAWN,
                    from = square,
                    destination = game.enPassantTargetSquare,
                )
            } else if (
                piece.color == BLACK &&
                (
                        game.enPassantTargetSquare == square.down()?.right() ||
                                game.enPassantTargetSquare == square.down()?.left()
                        )
            ) {
                return Move(
                    piece = piece,
                    capturedPiece = Piece.WHITE_PAWN,
                    from = square,
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
        getPromotedPieces(oneSquareMove).map {
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


    private fun getPromotedPieces(newPosition: Square): List<Piece> {
        return if (piece.color == WHITE && newPosition.rank == RANK_8) {
            listOf(Piece.WHITE_QUEEN, Piece.WHITE_ROOK, Piece.WHITE_BISHOP, Piece.WHITE_KNIGHT)
        } else if (piece.color == BLACK && newPosition.rank == RANK_1) {
            listOf(Piece.BLACK_QUEEN, Piece.BLACK_ROOK, Piece.BLACK_BISHOP, Piece.BLACK_KNIGHT)
        } else {
            emptyList()
        }
    }

    private fun getPawnDiagonalMoves(
        chessboard: Chessboard,
        diagonalFunction: (Square) -> Square?
    ): Move? {
        val diagonalSquare = diagonalFunction.invoke(square)
        if (diagonalSquare != null) {
            val pieceAtDiagonal = chessboard.getPositionAt(diagonalSquare)
            if (pieceAtDiagonal != null && pieceAtDiagonal.piece.color == piece.color.opposite()) {
                return Move(
                    piece = piece,
                    from = square,
                    destination = diagonalSquare,
                    capturedPiece = pieceAtDiagonal.piece,
                )
            }
        }
        return null
    }

    private fun getQueenPseudoLegalMoves(chessboard: Chessboard): List<Move> =
        getBishopPseudoLegalMoves(chessboard).plus(getRookPseudoLegalMoves(chessboard))

    private fun getRookPseudoLegalMoves(chessboard: Chessboard): List<Move> {
        return getLegalMovesFollowingDirection( fromPosition = this, chessboard = chessboard) { it.up() } +
                getLegalMovesFollowingDirection(fromPosition = this, chessboard = chessboard) { it.down() } +
                getLegalMovesFollowingDirection(fromPosition = this, chessboard = chessboard) { it.left() } +
                getLegalMovesFollowingDirection(fromPosition = this, chessboard = chessboard) { it.right() }
    }

    private fun getBishopPseudoLegalMoves(chessboard: Chessboard): List<Move> {
        return getLegalMovesFollowingDirection(  fromPosition = this, chessboard = chessboard) { it.upLeft() } +
                getLegalMovesFollowingDirection( fromPosition = this, chessboard = chessboard) { it.upRight() } +
                getLegalMovesFollowingDirection( fromPosition = this, chessboard = chessboard) { it.downLeft() } +
                getLegalMovesFollowingDirection( fromPosition = this, chessboard = chessboard) { it.downRight() }
    }

    private fun getKingPseudoLegalMoves(game: Game): List<Move> {
        return listOfNotNull(
           square.up(),
           square.down(),
           square.left(),
           square.right(),
           square.upLeft(),
           square.upRight(),
           square.downRight(),
           square.downLeft()
        )
            .filterNot { game.chessboard.isPiecePresentAtAndHasColor(it, piece.color) }
            .map {
                Move(
                    piece = piece,
                    from = square,
                    destination = it,
                    capturedPiece = game.chessboard.getPositionAt(it)?.piece
                )
            }.plus(getCastleMoveIfPossible(game))
    }

    private fun getCastleMoves(
        game: Game,
        isKingCastlePossible: Boolean,
        isQueenCastlePossible: Boolean,
        kingDestinationKingCastle: Square,
        kingDestinationQueenCastle: Square,
        kingCastlingPathSquares: List<Square>,
        queenCastlingPathSquares: List<Square>,
    ): List<Move> {
        val isTherePiecesOnQueenCastlingPath = game.chessboard.piecesOnBoard.any { queenCastlingPathSquares.minus(square).contains(it.square) }
        val isTherePiecesOnKingCastlingPath = game.chessboard.piecesOnBoard.any { kingCastlingPathSquares.minus(square).contains(it.square) }
        // We don't need to check if opponent can castle from this position, we just want to check if one of its legal moves can threaten one of king
        // castling path squares. Furthermore, considering castle here would create an infinite loop / stack overflow.
        val gameWithoutCastling = game.copy(
            castling = Castling(
                isWhiteKingCastlePossible = false,
                isWhiteQueenCastlePossible = false,
                isBlackQueenCastlePossible = false,
                isBlackKingCastlePossible = false
            ),
        )
        val allPseudoLegalMovesForPlayer = game.chessboard.getAllPseudoLegalMovesForColor(piece.color.opposite(), gameWithoutCastling)
        val hasKingCastleSquaresUnderAttack: Boolean = allPseudoLegalMovesForPlayer.any { kingCastlingPathSquares.contains(it.destination) }
        val hasQueenCastleSquaresUnderAttack: Boolean = allPseudoLegalMovesForPlayer.any { queenCastlingPathSquares.contains(it.destination) }

        return listOfNotNull(
            if (isKingCastlePossible && !isTherePiecesOnKingCastlingPath && !hasKingCastleSquaresUnderAttack) {
                Move(
                    piece = piece,
                    from = square,
                    destination = kingDestinationKingCastle,
                    isKingCastle = true,
                )
            } else null,
            if (isQueenCastlePossible && !isTherePiecesOnQueenCastlingPath && !hasQueenCastleSquaresUnderAttack) {
                Move(
                    piece = piece,
                    from = square,
                    destination = kingDestinationQueenCastle,
                    isQueenCastle = true,
                )
            } else null
        )
    }

    private fun getCastleMoveIfPossible(game: Game): List<Move> {
        return if (game.castling.isCastlingPossibleForWhite && game.sideToMove == WHITE ) {
            getCastleMoves(
                game = game,
                isKingCastlePossible = game.castling.isWhiteKingCastlePossible,
                isQueenCastlePossible = game.castling.isWhiteQueenCastlePossible,
                kingDestinationKingCastle = Square.G1,
                kingDestinationQueenCastle = Square.C1,
                kingCastlingPathSquares = listOf(Square.E1, Square.F1, Square.G1),
                queenCastlingPathSquares = listOf(Square.E1, Square.D1, Square.C1),
            )
        } else if (game.castling.isCastlingPossibleForBlack && game.sideToMove == BLACK) {
            getCastleMoves(
                game = game,
                isKingCastlePossible = game.castling.isBlackKingCastlePossible,
                isQueenCastlePossible = game.castling.isBlackQueenCastlePossible,
                kingDestinationKingCastle = Square.G8,
                kingDestinationQueenCastle = Square.C8,
                kingCastlingPathSquares = listOf(Square.E8, Square.F8, Square.G8),
                queenCastlingPathSquares = listOf(Square.E8, Square.D8, Square.C8),
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