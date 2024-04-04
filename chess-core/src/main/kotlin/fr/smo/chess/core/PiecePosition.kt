package fr.smo.chess.core

import fr.smo.chess.core.Color.BLACK
import fr.smo.chess.core.Color.WHITE
import fr.smo.chess.core.Rank.*
import fr.smo.chess.core.utils.ifTrue

data class PiecePosition(val square: Square, val piece: Piece) {

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
                    origin = square,
                    destination = it,
                    capturedPiece = chessboard.getPositionAt(it)?.piece,
                )
            }
    }


    private fun getPawnPseudoLegalMoves(game: Game): List<Move> =
        (if (piece.color == WHITE) {
            getPawnOneSquareAndTwoSquaresMoves(
                chessboard = game.chessboard,
                pawnPiecePosition = this,
                hasPawnNotAlreadyMovedFromInitialPosition = square.rank == RANK_2,
                pawnAdvanceFunction = { it.up() },
            ).plus(getPawnDiagonalMoves(game.chessboard) { it.upRight() })
             .plus(getPawnDiagonalMoves(game.chessboard) { it.upLeft() })
        } else {
            getPawnOneSquareAndTwoSquaresMoves(
                chessboard = game.chessboard,
                pawnPiecePosition = this,
                hasPawnNotAlreadyMovedFromInitialPosition = square.rank == RANK_7,
                pawnAdvanceFunction = { it.down() },
            ).plus(getPawnDiagonalMoves(game.chessboard) { it.downRight() }
            ).plus(getPawnDiagonalMoves(game.chessboard) { it.downLeft() })
        }.plus(getPawnEnPassantMove(game))).filterNotNull()


    private fun getPawnEnPassantMove(game: Game): Move? {
        return game.enPassantTargetSquare?.let { enPassantTargetSquare->
            if (piece.color == WHITE && (enPassantTargetSquare == square.up()?.right() || enPassantTargetSquare == square.up()?.left())) {
                return Move(
                    piece = piece,
                    capturedPiece = Piece.BLACK_PAWN,
                    origin = square,
                    destination = game.enPassantTargetSquare,
                )
            } else if (piece.color == BLACK && (enPassantTargetSquare == square.down()?.right() || enPassantTargetSquare == square.down()?.left())) {
                return Move(
                    piece = piece,
                    capturedPiece = Piece.WHITE_PAWN,
                    origin = square,
                    destination = enPassantTargetSquare,
                )
            } else null
        }
    }

    private fun getPawnOneSquareAndTwoSquaresMoves(
        chessboard: Chessboard,
        pawnPiecePosition: PiecePosition,
        hasPawnNotAlreadyMovedFromInitialPosition: Boolean,
        pawnAdvanceFunction: (Square) -> Square?
    ): List<Move> {
        return pawnAdvanceFunction.invoke(pawnPiecePosition.square)?.let { oneSquareMove ->
            chessboard.getPositionAt(oneSquareMove)?.let{ emptyList() } ?:
                getOneSquarePawnMoves(oneSquareMove)
                    .plus(getPawnTwoSquaresMove(
                        hasPawnNotAlreadyMoved = hasPawnNotAlreadyMovedFromInitialPosition,
                        pawnFrontMoveFunction = pawnAdvanceFunction,
                        chessboard = chessboard,
                        pawnPiecePosition = pawnPiecePosition
                    ))
                    .filterNotNull()
        } ?: emptyList()
    }

    private fun getPawnTwoSquaresMove(
        hasPawnNotAlreadyMoved: Boolean,
        pawnFrontMoveFunction: (Square) -> Square?,
        chessboard: Chessboard,
        pawnPiecePosition: PiecePosition
    ): Move? {
        return hasPawnNotAlreadyMoved.ifTrue {
            pawnFrontMoveFunction.invoke(pawnFrontMoveFunction.invoke(pawnPiecePosition.square)!!)
                ?.takeIf { chessboard.getPositionAt(it) == null }
                ?.let { squareTwoSquareAway ->
                    Move(piece = pawnPiecePosition.piece, origin = pawnPiecePosition.square, destination = squareTwoSquareAway)
                }
        }
    }

    private fun getOneSquarePawnMoves(destinationSquare: Square, pieceOnDestinationSquare : Piece? = null): List<Move> =
        getPromotedPieces(destinationSquare).map {
            Move(
                piece = piece,
                origin = square,
                destination = destinationSquare,
                promotedTo = it,
                capturedPiece = pieceOnDestinationSquare,
            )
        }.ifEmpty {
            listOf(
                Move(
                    piece = piece,
                    origin = square,
                    destination = destinationSquare,
                    capturedPiece = pieceOnDestinationSquare,
                )
            )
        }


    private fun getPromotedPieces(newPosition: Square): List<Piece> {
        return if (piece.color == WHITE && newPosition.rank == RANK_8) {
            Piece.entries.filter { it.color == WHITE }.filter { it.type.isPromotable }
        } else if (piece.color == BLACK && newPosition.rank == RANK_1) {
            Piece.entries.filter { it.color == BLACK }.filter { it.type.isPromotable }
        } else {
            emptyList()
        }
    }

    private fun getPawnDiagonalMoves(
        chessboard: Chessboard,
        diagonalFunction: (Square) -> Square?
    ): List<Move> {
        return diagonalFunction.invoke(square)?.let{diagonalSquare ->
            chessboard.getPositionAt(diagonalSquare)
                ?.takeIf { it.piece.color == piece.color.opposite() }
                ?.let { pieceAtDiagonal->
                    getOneSquarePawnMoves(destinationSquare = diagonalSquare, pieceOnDestinationSquare = pieceAtDiagonal.piece,)
            } ?: emptyList()
        } ?: emptyList()
    }

    private fun getQueenPseudoLegalMoves(chessboard: Chessboard): List<Move> =
        getBishopPseudoLegalMoves(chessboard) + getRookPseudoLegalMoves(chessboard)

    private fun getRookPseudoLegalMoves(chessboard: Chessboard): List<Move> {
        return getLegalMovesFollowingDirection(piecePosition = this, chessboard = chessboard) { it.up() } +
                getLegalMovesFollowingDirection(piecePosition = this, chessboard = chessboard) { it.down() } +
                getLegalMovesFollowingDirection(piecePosition = this, chessboard = chessboard) { it.left() } +
                getLegalMovesFollowingDirection(piecePosition = this, chessboard = chessboard) { it.right() }
    }

    private fun getBishopPseudoLegalMoves(chessboard: Chessboard): List<Move> {
        return getLegalMovesFollowingDirection(piecePosition = this, chessboard = chessboard) { it.upLeft() } +
                getLegalMovesFollowingDirection(piecePosition = this, chessboard = chessboard) { it.upRight() } +
                getLegalMovesFollowingDirection(piecePosition = this, chessboard = chessboard) { it.downLeft() } +
                getLegalMovesFollowingDirection(piecePosition = this, chessboard = chessboard) { it.downRight() }
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
                    origin = square,
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
                    origin = square,
                    destination = kingDestinationKingCastle,
                    isKingCastle = true,
                )
            } else null,
            if (isQueenCastlePossible && !isTherePiecesOnQueenCastlingPath && !hasQueenCastleSquaresUnderAttack) {
                Move(
                    piece = piece,
                    origin = square,
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
        piecePosition: PiecePosition,
        currentSquare: Square = piecePosition.square,
        chessboard: Chessboard,
        direction: (origin: Square) -> Square?
    ): List<Move> {
        return direction.invoke(currentSquare)?.let { newSquare ->
            chessboard.getPositionAt(newSquare)?.let { newPosition ->
                if (newPosition.piece.color != piecePosition.piece.color) {
                    listOf(
                        Move(piece = piecePosition.piece, origin = piecePosition.square, destination = newSquare, capturedPiece = newPosition.piece)
                    )
                } else
                    emptyList()
            } ?: (
                    getLegalMovesFollowingDirection(piecePosition, newSquare, chessboard, direction).plus(
                        Move(piece = piecePosition.piece, origin = piecePosition.square, destination = newSquare)
                    ))
            } ?: emptyList()
        }
}