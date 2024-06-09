package fr.smo.chess.core.algs

import fr.smo.chess.core.*
import fr.smo.chess.core.utils.ifTrue
import kotlin.random.Random

/**
 * In Pseudo-legal move generation pieces obey their normal rules of movement, but they're not checked beforehand to see
 * if they'll leave the king in check. It is left up to the move-making function to test the move, or it is even possible
 * to let the king remain in check and only test for the capture of the king on the next move
 */
fun getPseudoLegalMoves(position: Position, piecePosition: PiecePosition): List<Move> {
    return when (piecePosition.pieceType) {
        PieceType.KING -> getKingPseudoLegalMoves(position, piecePosition)
        PieceType.PAWN -> getPawnPseudoLegalMoves(position, piecePosition)
        PieceType.BISHOP -> getBishopPseudoLegalMoves(position.chessboard, piecePosition)
        PieceType.KNIGHT -> getKnightPseudoLegalMoves(position.chessboard, piecePosition)
        PieceType.QUEEN -> getQueenPseudoLegalMoves(position.chessboard, piecePosition)
        PieceType.ROOK -> getRookPseudoLegalMoves(position.chessboard, piecePosition)
    }
}

fun getPseudoLegalMovesRegardingDestination(position: Position, destinationPiecePosition: PiecePosition): List<Move> {
    return position.chessboard.getPiecePositions(color = position.sideToMove)
            .filter { it.pieceType == destinationPiecePosition.pieceType }
            .parallelStream()
            .flatMap { getPseudoLegalMoves(position, it).stream() }
            .toList()
}

fun getAllPseudoLegalMovesForColor(color: Color, position: Position): List<Move> {
    return position.chessboard.getPiecePositions(color).parallelStream().flatMap {
        getPseudoLegalMoves(position, it).stream()
    }.toList()
}

fun hasAPseudoLegalMovesSatisfying(color: Color, position: Position, predicate: (Move) -> Boolean): Boolean {
    return getFirstPseudoLegalMoveSatisfying(color, position,null,  predicate) != null
}

fun getFirstPseudoLegalMoveSatisfying(color: Color, position: Position, random : Random? = null, predicate: (Move) -> Boolean): Move? {
    position.chessboard.getPiecePositions(color)
            .let { if (random != null) it.shuffled(random) else it }
            .forEach {
                getPseudoLegalMoves(position, it).forEach { move ->
                    if (predicate.invoke(move)) {
                        return move
                    }
                }
            }
    return null
}


private fun getKnightPseudoLegalMoves(chessboard: Chessboard, knightPosition: PiecePosition): List<Move> {
    return listOfNotNull(
            knightPosition.square.up()?.upLeft(),
            knightPosition.square.up()?.upRight(),
            knightPosition.square.down()?.downRight(),
            knightPosition.square.down()?.downLeft(),
            knightPosition.square.left()?.upLeft(),
            knightPosition.square.left()?.downLeft(),
            knightPosition.square.right()?.upRight(),
            knightPosition.square.right()?.downRight(),
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


private fun getPawnPseudoLegalMoves(position: Position, pawnPosition: PiecePosition): List<Move> =
        (if (pawnPosition.color == Color.WHITE) {
            getPawnOneSquareAndTwoSquaresMoves(
                    chessboard = position.chessboard,
                    pawnPiecePosition = pawnPosition,
                    hasPawnNotAlreadyMovedFromInitialPosition = pawnPosition.square.rank == Rank.RANK_2,
                    pawnAdvanceFunction = { it.up() },
            ).plus(getPawnDiagonalMoves(position.chessboard, pawnPosition) { it.upRight() })
                    .plus(getPawnDiagonalMoves(position.chessboard, pawnPosition) { it.upLeft() })
        } else {
            getPawnOneSquareAndTwoSquaresMoves(
                    chessboard = position.chessboard,
                    pawnPiecePosition = pawnPosition,
                    hasPawnNotAlreadyMovedFromInitialPosition = pawnPosition.square.rank == Rank.RANK_7,
                    pawnAdvanceFunction = { it.down() },
            ).plus(getPawnDiagonalMoves(position.chessboard, pawnPosition) { it.downRight() }
            ).plus(getPawnDiagonalMoves(position.chessboard, pawnPosition) { it.downLeft() })
        }.plus(getPawnEnPassantMove(position, pawnPosition))).filterNotNull()


private fun getPawnEnPassantMove(position: Position, pawnPosition: PiecePosition): Move? {
    return position.enPassantTargetSquare?.let { enPassantTargetSquare->
        if (pawnPosition.color == Color.WHITE && (enPassantTargetSquare == pawnPosition.square.upRight() || enPassantTargetSquare == pawnPosition.square.upLeft())) {
            return Move(
                    piece = pawnPosition.piece,
                    capturedPiece = Piece.BLACK_PAWN,
                    origin = pawnPosition.square,
                    destination = position.enPassantTargetSquare,
            )
        } else if (pawnPosition.color == Color.BLACK && (enPassantTargetSquare == pawnPosition.square.downRight() || enPassantTargetSquare == pawnPosition.square.downLeft())) {
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
                .plus(
                    getPawnTwoSquaresMove(
                        hasPawnNotAlreadyMoved = hasPawnNotAlreadyMovedFromInitialPosition,
                        pawnFrontMoveFunction = pawnAdvanceFunction,
                        chessboard = chessboard,
                        pawnPiecePosition = pawnPiecePosition
                )
                )
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

private fun getKingPseudoLegalMoves(position: Position, kingPosition: PiecePosition): List<Move> {
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
            .filterNot { position.chessboard.isPiecePresentAtAndHasColor(it, kingPosition.color) }
            .map {
                Move(
                        piece = kingPosition.piece,
                        origin = kingPosition.square,
                        destination = it,
                        capturedPiece = position.chessboard.getPieceAt(it)
                )
            }.plus(getCastleMoveIfPossible(position, kingPosition))
}

private fun getCastleMove(
    position: Position,
    kingPosition: PiecePosition,
    isKingCastle : Boolean,
    isCurrentCastlePossible: Boolean,
    kingDestination: Square,
    kingCastlingPathSquares: List<Square>,
    squaresBetweenKingAndRook: List<Square>
) : Move? {
    if (!isCurrentCastlePossible) { return null }
    if (position.chessboard.hasAtLeastOnePieceAt(squaresBetweenKingAndRook)) { return null }
    val hasPieceAttackingKingPath = hasAPseudoLegalMovesSatisfying(kingPosition.color.opposite(), position.copyWithoutCastling()) {
        kingCastlingPathSquares.contains(it.destination)
    }
    return if (!hasPieceAttackingKingPath) {
        Move(
                piece = kingPosition.piece,
                origin = kingPosition.square,
                destination = kingDestination,
                isKingCastle = isKingCastle,
                isQueenCastle = !isKingCastle,
        )
    } else null
}

private fun Position.copyWithoutCastling() = copy(
        castling = Castling(
                isWhiteKingCastlePossible = false,
                isWhiteQueenCastlePossible = false,
                isBlackQueenCastlePossible = false,
                isBlackKingCastlePossible = false
        ),
)

private fun getCastleMoveIfPossible(position: Position, kingPosition: PiecePosition): List<Move> {

    return if (position.sideToMove == Color.WHITE) {
        listOfNotNull(
                getCastleMove(
                        position = position,
                        kingPosition = kingPosition,
                        isKingCastle = true,
                        isCurrentCastlePossible = position.castling.isWhiteKingCastlePossible,
                        kingDestination = Square.G1,
                        kingCastlingPathSquares = listOf(Square.E1, Square.F1, Square.G1),
                        squaresBetweenKingAndRook = listOf(Square.F1, Square.G1),
                ),
                getCastleMove(
                        position = position,
                        kingPosition = kingPosition,
                        isKingCastle = false,
                        isCurrentCastlePossible = position.castling.isWhiteQueenCastlePossible,
                        kingDestination = Square.C1,
                        kingCastlingPathSquares = listOf(Square.E1, Square.D1, Square.C1),
                        squaresBetweenKingAndRook = listOf(Square.B1, Square.C1, Square.D1),
                )
        )
    } else {
        listOfNotNull(
                getCastleMove(
                        position = position,
                        kingPosition = kingPosition,
                        isKingCastle = true,
                        isCurrentCastlePossible = position.castling.isBlackKingCastlePossible,
                        kingDestination = Square.G8,
                        kingCastlingPathSquares = listOf(Square.E8, Square.F8, Square.G8),
                        squaresBetweenKingAndRook = listOf(Square.F8, Square.G8),
                ),
                getCastleMove(
                        position = position,
                        kingPosition = kingPosition,
                        isKingCastle = false,
                        isCurrentCastlePossible = position.castling.isBlackQueenCastlePossible,
                        kingDestination = Square.C8,
                        kingCastlingPathSquares = listOf(Square.E8, Square.D8, Square.C8),
                        squaresBetweenKingAndRook = listOf(Square.B8, Square.C8, Square.D8),
                )
        )
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