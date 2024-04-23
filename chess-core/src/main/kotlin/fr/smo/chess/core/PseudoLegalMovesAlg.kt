package fr.smo.chess.core

import fr.smo.chess.core.utils.ifTrue
import kotlin.random.Random

/**
 * In Pseudo-legal move generation pieces obey their normal rules of movement, but they're not checked beforehand to see
 * if they'll leave the king in check. It is left up to the move-making function to test the move, or it is even possible
 * to let the king remain in check and only test for the capture of the king on the next move
 */
fun getPseudoLegalMoves(game: Game, piecePosition: PiecePosition): List<Move> {
    return when (piecePosition.pieceType) {
        PieceType.PAWN -> getPawnPseudoLegalMoves(game, piecePosition)
        PieceType.BISHOP -> getBishopPseudoLegalMoves(game.chessboard, piecePosition)
        PieceType.KNIGHT -> getKnightPseudoLegalMoves(game.chessboard, piecePosition)
        PieceType.QUEEN -> getQueenPseudoLegalMoves(game.chessboard, piecePosition)
        PieceType.KING -> getKingPseudoLegalMoves(game, piecePosition)
        PieceType.ROOK -> getRookPseudoLegalMoves(game.chessboard, piecePosition)
    }
}

fun getAllPseudoLegalMovesForColor(color: Color, game: Game): List<Move> {
    return game.chessboard.getPiecePositions(color). flatMap {
        getPseudoLegalMoves(game, it)
    }
}

fun hasAPseudoLegalMovesSatisfying(color: Color, game: Game, predicate: (Move) -> Boolean): Boolean {
    return getFirstPseudoLegalMoveSatisfying(color, game,null,  predicate) != null
}

fun getFirstPseudoLegalMoveSatisfying(color: Color, game: Game, random : Random? = null, predicate: (Move) -> Boolean): Move? {
    game.chessboard.getPiecePositions(color)
            .let { if (random != null) it.shuffled(random) else it }
            .forEach {
                getPseudoLegalMoves(game, it).forEach { move ->
                    if (predicate.invoke(move)) {
                        return move
                    }
                }
            }
    return null
}


private fun getKnightPseudoLegalMoves(chessboard: Chessboard, knightPosition: PiecePosition): List<Move> {
    return listOfNotNull(
            knightPosition.square.up()?.up()?.left(),
            knightPosition.square.up()?.up()?.right(),
            knightPosition.square.down()?.down()?.right(),
            knightPosition.square.down()?.down()?.left(),
            knightPosition.square.left()?.left()?.up(),
            knightPosition.square.left()?.left()?.down(),
            knightPosition.square.right()?.right()?.up(),
            knightPosition.square.right()?.right()?.down(),
    ).filterNot { chessboard.isPiecePresentAtAndHasColor(it, knightPosition.color) }
            .map {
                Move(
                        piece = knightPosition.piece,
                        origin = knightPosition.square,
                        destination = it,
                        capturedPiece = chessboard.getPieceAt(it),
                )
            }
}


private fun getPawnPseudoLegalMoves(game: Game, pawnPosition: PiecePosition): List<Move> =
        (if (pawnPosition.color == Color.WHITE) {
            getPawnOneSquareAndTwoSquaresMoves(
                    chessboard = game.chessboard,
                    pawnPiecePosition = pawnPosition,
                    hasPawnNotAlreadyMovedFromInitialPosition = pawnPosition.square.rank == Rank.RANK_2,
                    pawnAdvanceFunction = { it.up() },
            ).plus(getPawnDiagonalMoves(game.chessboard, pawnPosition) { it.upRight() })
                    .plus(getPawnDiagonalMoves(game.chessboard, pawnPosition) { it.upLeft() })
        } else {
            getPawnOneSquareAndTwoSquaresMoves(
                    chessboard = game.chessboard,
                    pawnPiecePosition = pawnPosition,
                    hasPawnNotAlreadyMovedFromInitialPosition = pawnPosition.square.rank == Rank.RANK_7,
                    pawnAdvanceFunction = { it.down() },
            ).plus(getPawnDiagonalMoves(game.chessboard, pawnPosition) { it.downRight() }
            ).plus(getPawnDiagonalMoves(game.chessboard, pawnPosition) { it.downLeft() })
        }.plus(getPawnEnPassantMove(game, pawnPosition))).filterNotNull()


private fun getPawnEnPassantMove(game: Game, pawnPosition: PiecePosition): Move? {
    return game.enPassantTargetSquare?.let { enPassantTargetSquare->
        if (pawnPosition.color == Color.WHITE && (enPassantTargetSquare == pawnPosition.square.up()?.right() || enPassantTargetSquare == pawnPosition.square.up()?.left())) {
            return Move(
                    piece = pawnPosition.piece,
                    capturedPiece = Piece.BLACK_PAWN,
                    origin = pawnPosition.square,
                    destination = game.enPassantTargetSquare,
            )
        } else if (pawnPosition.color == Color.BLACK && (enPassantTargetSquare == pawnPosition.square.down()?.right() || enPassantTargetSquare == pawnPosition.square.down()?.left())) {
            return Move(
                    piece = pawnPosition.piece,
                    capturedPiece = Piece.WHITE_PAWN,
                    origin = pawnPosition.square,
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
    return pawnAdvanceFunction.invoke(pawnPiecePosition.square)?.let { pawnDestination ->
        chessboard.getPieceAt(pawnDestination)?.let{ emptyList() } ?:
        getOneSquarePawnMoves(pawnPiecePosition, pawnDestination)
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
                ?.takeIf { chessboard.getPieceAt(it) == null }
                ?.let { squareTwoSquareAway ->
                    Move(piece = pawnPiecePosition.piece, origin = pawnPiecePosition.square, destination = squareTwoSquareAway)
                }
    }
}

private fun getOneSquarePawnMoves(pawnPosition: PiecePosition, destinationSquare: Square, pieceOnDestinationSquare : Piece? = null): List<Move> =
        getPromotedPieces(pawnPosition, destinationSquare).map {
            Move(
                    piece = pawnPosition.piece,
                    origin = pawnPosition.square,
                    destination = destinationSquare,
                    promotedTo = it,
                    capturedPiece = pieceOnDestinationSquare,
            )
        }.ifEmpty {
            listOf(
                    Move(
                            piece = pawnPosition.piece,
                            origin = pawnPosition.square,
                            destination = destinationSquare,
                            capturedPiece = pieceOnDestinationSquare,
                    )
            )
        }


private fun getPromotedPieces(pawnPosition: PiecePosition, destinationSquare: Square): List<Piece> {
    return if (pawnPosition.piece == Piece.WHITE_PAWN && destinationSquare.rank == Rank.RANK_8) {
        Piece.entries.filter { it.color == Color.WHITE }.filter { it.type.isPromotable }
    } else if (pawnPosition.piece == Piece.BLACK_PAWN && destinationSquare.rank == Rank.RANK_1) {
        Piece.entries.filter { it.color == Color.BLACK }.filter { it.type.isPromotable }
    } else {
        emptyList()
    }
}

private fun getPawnDiagonalMoves(
        chessboard: Chessboard,
        pawnPosition: PiecePosition,
        diagonalFunction: (Square) -> Square?
): List<Move> {
    return diagonalFunction.invoke(pawnPosition.square)?.let{diagonalSquare -> chessboard.getPieceAt(diagonalSquare)
            ?.takeIf { it.color == pawnPosition.color.opposite() }
            ?.let { pieceAtDiagonal->
                getOneSquarePawnMoves(pawnPosition = pawnPosition, destinationSquare = diagonalSquare, pieceOnDestinationSquare = pieceAtDiagonal)
            } ?: emptyList()
    } ?: emptyList()
}

private fun getQueenPseudoLegalMoves(chessboard: Chessboard, queenPosition: PiecePosition): List<Move> =
        getBishopPseudoLegalMoves(chessboard, queenPosition) + getRookPseudoLegalMoves(chessboard, queenPosition)

private fun getRookPseudoLegalMoves(chessboard: Chessboard, rookPosition: PiecePosition): List<Move> {
    return getLegalMovesFollowingDirection(piecePosition = rookPosition, chessboard = chessboard) { it.up() } +
            getLegalMovesFollowingDirection(piecePosition = rookPosition, chessboard = chessboard) { it.down() } +
            getLegalMovesFollowingDirection(piecePosition = rookPosition, chessboard = chessboard) { it.left() } +
            getLegalMovesFollowingDirection(piecePosition = rookPosition, chessboard = chessboard) { it.right() }
}

private fun getBishopPseudoLegalMoves(chessboard: Chessboard, bishopPosition: PiecePosition): List<Move> {
    return getLegalMovesFollowingDirection(piecePosition = bishopPosition, chessboard = chessboard) { it.upLeft() } +
            getLegalMovesFollowingDirection(piecePosition = bishopPosition, chessboard = chessboard) { it.upRight() } +
            getLegalMovesFollowingDirection(piecePosition = bishopPosition, chessboard = chessboard) { it.downLeft() } +
            getLegalMovesFollowingDirection(piecePosition = bishopPosition, chessboard = chessboard) { it.downRight() }
}

private fun getKingPseudoLegalMoves(game: Game, kingPosition: PiecePosition): List<Move> {
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
            .filterNot { game.chessboard.isPiecePresentAtAndHasColor(it, kingPosition.color) }
            .map {
                Move(
                        piece = kingPosition.piece,
                        origin = kingPosition.square,
                        destination = it,
                        capturedPiece = game.chessboard.getPieceAt(it)
                )
            }.plus(getCastleMoveIfPossible(game, kingPosition))
}

private fun getCastleMoves(
        game: Game,
        kingPosition: PiecePosition,
        isKingCastlePossible: Boolean,
        isQueenCastlePossible: Boolean,
        kingDestinationKingCastle: Square,
        kingDestinationQueenCastle: Square,
        kingCastlingPathSquares: List<Square>,
        queenCastlingPathSquares: List<Square>,
): List<Move> {
    val isTherePiecesOnQueenCastlingPath = game.chessboard.hasAtLeastOnePieceAt(queenCastlingPathSquares.minus(kingPosition.square))
    val isTherePiecesOnKingCastlingPath = game.chessboard.hasAtLeastOnePieceAt( kingCastlingPathSquares.minus(kingPosition.square))
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

    val hasKingCastleSquaresUnderAttack = hasAPseudoLegalMovesSatisfying(
            kingPosition.color.opposite(),  gameWithoutCastling
    ) { kingCastlingPathSquares.contains(it.destination) }

    val hasQueenCastleSquaresUnderAttack = hasAPseudoLegalMovesSatisfying(
            kingPosition.color.opposite(),  gameWithoutCastling
    ) { queenCastlingPathSquares.contains(it.destination) }

    return listOfNotNull(
            if (isKingCastlePossible && !isTherePiecesOnKingCastlingPath && !hasKingCastleSquaresUnderAttack) {
                Move(
                        piece = kingPosition.piece,
                        origin = kingPosition.square,
                        destination = kingDestinationKingCastle,
                        isKingCastle = true,
                )
            } else null,
            if (isQueenCastlePossible && !isTherePiecesOnQueenCastlingPath && !hasQueenCastleSquaresUnderAttack) {
                Move(
                        piece = kingPosition.piece,
                        origin = kingPosition.square,
                        destination = kingDestinationQueenCastle,
                        isQueenCastle = true,
                )
            } else null
    )
}

private fun getCastleMoveIfPossible(game: Game, kingPosition: PiecePosition): List<Move> {
    return if (game.castling.isCastlingPossibleForWhite && game.sideToMove == Color.WHITE) {
        getCastleMoves(
                game = game,
                kingPosition = kingPosition,
                isKingCastlePossible = game.castling.isWhiteKingCastlePossible,
                isQueenCastlePossible = game.castling.isWhiteQueenCastlePossible,
                kingDestinationKingCastle = Square.G1,
                kingDestinationQueenCastle = Square.C1,
                kingCastlingPathSquares = listOf(Square.E1, Square.F1, Square.G1),
                queenCastlingPathSquares = listOf(Square.E1, Square.D1, Square.C1),
        )
    } else if (game.castling.isCastlingPossibleForBlack && game.sideToMove == Color.BLACK) {
        getCastleMoves(
                game = game,
                kingPosition = kingPosition,
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
        chessboard.getPieceAt(newSquare)?.let { pieceAtNewSquare ->
            if (pieceAtNewSquare.color != piecePosition.piece.color) {
                listOf(
                        Move(piece = piecePosition.piece, origin = piecePosition.square, destination = newSquare, capturedPiece = pieceAtNewSquare)
                )
            } else
                emptyList()
        } ?: (
                getLegalMovesFollowingDirection(piecePosition, newSquare, chessboard, direction).plus(
                        Move(piece = piecePosition.piece, origin = piecePosition.square, destination = newSquare)
                ))
    } ?: emptyList()
}