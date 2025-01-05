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
    return getFirstPseudoLegalMoveSatisfying(color, position, null, predicate) != null
}

fun getFirstPseudoLegalMoveSatisfying(
    color: Color,
    position: Position,
    random: Random? = null,
    predicate: (Move) -> Boolean
): Move? {
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


private fun getPawnPseudoLegalMoves(position: Position, pawnPosition: PiecePosition): List<Move> {
    return getPawnOneSquareAndTwoSquaresMoves(chessboard = position.chessboard, pawnPiecePosition = pawnPosition)
        .plus(getPawnDiagonalsMoves(chessboard = position.chessboard, pawnPosition = pawnPosition))
        .plus(getPawnEnPassantMove(position = position, pawnPosition = pawnPosition))
        .filterNotNull()
}


private fun getPawnEnPassantMove(position: Position, pawnPosition: PiecePosition): Move? {
    if (position.enPassantTargetSquare == null) {
        return null
    }
    return pawnPosition.color.getPawnDiagonalSquareFunction()
        .firstOrNull { it.invoke(pawnPosition.square) == position.enPassantTargetSquare }?.let {
            Move(
                piece = pawnPosition.piece,
                capturedPiece = when (pawnPosition.color) {
                    Color.WHITE -> Piece.BLACK_PAWN
                    Color.BLACK -> Piece.WHITE_PAWN
                },
                origin = pawnPosition.square,
                destination = position.enPassantTargetSquare,
            )
        }
}

private fun getPawnOneSquareAndTwoSquaresMoves(
    chessboard: Chessboard,
    pawnPiecePosition: PiecePosition,
): List<Move> {
    return pawnPiecePosition.color.getPawnAdvanceFunction().invoke(pawnPiecePosition.square)?.let { pawnDestination ->
        if (chessboard.isPiecePresentAt(pawnDestination)) {
            emptyList()
        } else {
            getOneSquarePawnMoves(pawnPiecePosition, pawnDestination)
                .plus(getPawnTwoSquaresMove(chessboard = chessboard, pawnPiecePosition = pawnPiecePosition))
                .filterNotNull()
        }
    } ?: emptyList()
}

private fun getPawnTwoSquaresMove(
    chessboard: Chessboard,
    pawnPiecePosition: PiecePosition
): Move? {
    val hasPawnNotAlreadyMoved = pawnPiecePosition.square.rank == pawnPiecePosition.color.getPawnStartingRank()
    return hasPawnNotAlreadyMoved.ifTrue {
        val destSquare =
            pawnPiecePosition.color.getPawnAdvanceFunction(advanceTwice = true).invoke(pawnPiecePosition.square)
        if (destSquare != null && chessboard.hasNoPiecePresentAt(destSquare)) {
            Move(piece = pawnPiecePosition.piece, origin = pawnPiecePosition.square, destination = destSquare)
        } else {
            null
        }
    }
}

private fun getOneSquarePawnMoves(
    pawnPosition: PiecePosition,
    destinationSquare: Square,
    pieceOnDestinationSquare: Piece? = null
): List<Move> {
    return getPromotedPieces(pawnPosition, destinationSquare).map {
        Move(
            piece = pawnPosition.piece,
            origin = pawnPosition.square,
            destination = destinationSquare,
            promotedTo = it,
            capturedPiece = pieceOnDestinationSquare
        )
    }.ifEmpty {
        listOf(
            Move(
                piece = pawnPosition.piece,
                origin = pawnPosition.square,
                destination = destinationSquare,
                capturedPiece = pieceOnDestinationSquare
            )
        )
    }
}


private fun getPromotedPieces(pawnPosition: PiecePosition, destinationSquare: Square): List<Piece> {
    return if (pawnPosition.piece.type == PieceType.PAWN && destinationSquare.rank == pawnPosition.color.getPromotionRank()) {
        Piece.entries.filter { it.color == pawnPosition.color }.filter { it.type.isPromotable }
    } else {
        emptyList()
    }
}

private fun getPawnDiagonalsMoves(
    chessboard: Chessboard,
    pawnPosition: PiecePosition,
): List<Move> {

    return pawnPosition.color.getPawnDiagonalSquareFunction()
        .mapNotNull { fn -> fn.invoke(pawnPosition.square) }
        .flatMap { diagonalAdvanceSquare ->
            chessboard.getPieceAt(diagonalAdvanceSquare)
                ?.takeIf { it.color == pawnPosition.color.opposite() }
                ?.let { pieceAtDiagonal ->
                    getOneSquarePawnMoves(
                        pawnPosition = pawnPosition,
                        destinationSquare = diagonalAdvanceSquare,
                        pieceOnDestinationSquare = pieceAtDiagonal
                    )
                } ?: emptyList()
        }
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
    castle: Castle
): Move? {
    if (!castle.isCastleStillPossible) {
        return null
    }
    val hasPieceOnRookOrKingPath = (castle.rookCastlingPathSquares + castle.kingCastlingPathSquares)
        .filter { it != castle.rookStartSquare }
        .filter { it != castle.kingStartSquare }
        .any { position.chessboard.getPieceAt(it) != null }
    if (hasPieceOnRookOrKingPath) {
        return null
    }
    val hasPieceAttackingKingPath =
        hasAPseudoLegalMovesSatisfying(kingPosition.color.opposite(), position.copyWithoutCastling()) {
            castle.kingCastlingPathSquares.contains(it.destination)
        }
    return if (!hasPieceAttackingKingPath) {
        Move(
            piece = kingPosition.piece,
            origin = kingPosition.square,
            destination = castle.kingDestinationSquare,
            isKingCastle = castle.castleType == CastleType.SHORT,
            isQueenCastle = castle.castleType == CastleType.LONG,
        )
    } else null
}

private fun Position.copyWithoutCastling() = copy(castles = Castles(castles = emptyList()),)

private fun getCastleMoveIfPossible(position: Position, kingPosition: PiecePosition): List<Move> {
    return position.castles.castles
        .filter { it.color == kingPosition.color }
        .mapNotNull {
            getCastleMove(
                position = position,
                kingPosition = kingPosition,
                castle = it,
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
                    Move(
                        piece = piecePosition.piece,
                        origin = piecePosition.square,
                        destination = newSquare,
                        capturedPiece = pieceAtNewSquare
                    )
                )
            } else
                emptyList()
        } ?: (
                getLegalMovesFollowingDirection(piecePosition, newSquare, chessboard, direction).plus(
                    Move(piece = piecePosition.piece, origin = piecePosition.square, destination = newSquare)
                ))
    } ?: emptyList()
}