package fr.smo.chess.core

import fr.smo.chess.core.utils.ifTrue


/**
 * Not good enough in terms of performance and has a lot of duplicated code with hasAPseudoLegalMovesSatisfying()
 */
fun hasAPseudoLegalMovesSatisfying2(color: Color, game: Game, predicate: (Move) -> Boolean): Boolean {
    return game.chessboard.getPiecePositions(color).any { piece->
        when (piece.pieceType) {
            PieceType.PAWN -> hasPawnPseudoLegalMovesSatisfying(game, piece, predicate)
            PieceType.BISHOP -> hasBishopPseudoLegalMovesSatisfying(game.chessboard, piece, predicate)
            PieceType.KNIGHT -> getKnightPseudoLegalMovesSatisfying(game.chessboard, piece, predicate)
            PieceType.QUEEN -> hasQueenPseudoLegalMovesSatisfying(game.chessboard, piece, predicate)
            PieceType.KING -> hasKingPseudoLegalMovesSatisfying(game, piece, predicate)
            PieceType.ROOK -> hasRookPseudoLegalMovesSatisfying(game.chessboard, piece, predicate)
        }
    }
}


private fun getKnightPseudoLegalMovesSatisfying(chessboard: Chessboard, knightPosition: PiecePosition, predicate: (Move) -> Boolean): Boolean {
    return listOf(
            { knightPosition.square.up()?.upLeft() },
            { knightPosition.square.up()?.upRight() },
            { knightPosition.square.down()?.downRight() },
            { knightPosition.square.down()?.downLeft() },
            { knightPosition.square.left()?.upLeft() },
            { knightPosition.square.left()?.downLeft() },
            { knightPosition.square.right()?.upRight() },
            { knightPosition.square.right()?.downRight() },
    ).any { squareCreator ->
        squareCreator.invoke()?.let {
            if (!chessboard.isPiecePresentAtAndHasColor(it, knightPosition.color)) {
                val move = Move(piece = knightPosition.piece, origin = knightPosition.square, destination = it, capturedPiece = chessboard.getPieceAt(it))
                predicate.invoke(move)
            } else false
        } ?: false
    }
}


private fun hasPawnPseudoLegalMovesSatisfying(game: Game, pawnPosition: PiecePosition, predicate: (Move) -> Boolean): Boolean {
    if (pawnPosition.color == Color.WHITE) {
        hasPawnOneSquareAndTwoSquaresMovesSatisfying(
                chessboard = game.chessboard,
                pawnPiecePosition = pawnPosition,
                hasPawnNotAlreadyMovedFromInitialPosition = pawnPosition.square.rank == Rank.RANK_2,
                pawnAdvanceFunction = { it.up() },
                predicate = predicate,
        ).let { if (it) return true }
        hasPawnDiagonalMovesSatisfying(game.chessboard, pawnPosition, { it.upRight() }, predicate).let { if (it) return true }
        hasPawnDiagonalMovesSatisfying(game.chessboard, pawnPosition, { it.upLeft() }, predicate).let { if (it) return true }
    } else {
        hasPawnOneSquareAndTwoSquaresMovesSatisfying(
                chessboard = game.chessboard,
                pawnPiecePosition = pawnPosition,
                hasPawnNotAlreadyMovedFromInitialPosition = pawnPosition.square.rank == Rank.RANK_7,
                pawnAdvanceFunction = { it.down() },
                predicate = predicate,
        ).let { if (it) return true }
        hasPawnDiagonalMovesSatisfying(game.chessboard, pawnPosition, { it.downRight() }, predicate).let { if (it) return true }
        hasPawnDiagonalMovesSatisfying(game.chessboard, pawnPosition, { it.downLeft() }, predicate).let { if (it) return true }
    }
    hasPawnEnPassantMoveSatisfying(game, pawnPosition, predicate).let { if (it) return true }
    return false
}

private fun hasPawnEnPassantMoveSatisfying(game: Game, pawnPosition: PiecePosition, predicate: (Move) -> Boolean): Boolean {
    return game.enPassantTargetSquare?.let { enPassantTargetSquare ->
        if (pawnPosition.color == Color.WHITE && (enPassantTargetSquare == pawnPosition.square.upRight() || enPassantTargetSquare == pawnPosition.square.upLeft())) {
            predicate(Move(
                    piece = pawnPosition.piece,
                    capturedPiece = Piece.BLACK_PAWN,
                    origin = pawnPosition.square,
                    destination = game.enPassantTargetSquare,
            ))
        } else if (pawnPosition.color == Color.BLACK && (enPassantTargetSquare == pawnPosition.square.downRight() || enPassantTargetSquare == pawnPosition.square.downLeft())) {
            predicate(Move(
                    piece = pawnPosition.piece,
                    capturedPiece = Piece.WHITE_PAWN,
                    origin = pawnPosition.square,
                    destination = enPassantTargetSquare,
            ))
        } else false
    } ?: false
}

private fun hasPawnOneSquareAndTwoSquaresMovesSatisfying(
        chessboard: Chessboard,
        pawnPiecePosition: PiecePosition,
        hasPawnNotAlreadyMovedFromInitialPosition: Boolean,
        pawnAdvanceFunction: (Square) -> Square?,
        predicate: (Move) -> Boolean,
): Boolean {
    return pawnAdvanceFunction.invoke(pawnPiecePosition.square)?.let { pawnDestination ->
        chessboard.getPieceAt(pawnDestination)?.let { return false }
                ?: hasOneSquarePawnMovesSatisfying(pawnPiecePosition, pawnDestination, null, predicate).let { if (it) return true }
        return hasPawnTwoSquaresMoveSatisfying(
                hasPawnNotAlreadyMoved = hasPawnNotAlreadyMovedFromInitialPosition,
                pawnFrontMoveFunction = pawnAdvanceFunction,
                chessboard = chessboard,
                pawnPiecePosition = pawnPiecePosition,
                predicate = predicate
        )
    } ?: false
}

private fun hasPawnTwoSquaresMoveSatisfying(
        hasPawnNotAlreadyMoved: Boolean,
        pawnFrontMoveFunction: (Square) -> Square?,
        chessboard: Chessboard,
        pawnPiecePosition: PiecePosition,
        predicate: (Move) -> Boolean,
): Boolean {
    return hasPawnNotAlreadyMoved.ifTrue {
        pawnFrontMoveFunction.invoke(pawnFrontMoveFunction.invoke(pawnPiecePosition.square)!!)
                ?.takeIf { chessboard.getPieceAt(it) == null }
                ?.let { squareTwoSquareAway ->
                    predicate(Move(piece = pawnPiecePosition.piece, origin = pawnPiecePosition.square, destination = squareTwoSquareAway))
                }
    } ?: false
}

private fun hasOneSquarePawnMovesSatisfying(pawnPosition: PiecePosition, destinationSquare: Square, pieceOnDestinationSquare: Piece? = null, predicate: (Move) -> Boolean): Boolean {
    getPromotedPieces(pawnPosition, destinationSquare).forEach {
        Move(
                piece = pawnPosition.piece,
                origin = pawnPosition.square,
                destination = destinationSquare,
                promotedTo = it,
                capturedPiece = pieceOnDestinationSquare,
        ).let { move -> if (predicate(move)) return true }
    }
    Move(piece = pawnPosition.piece, origin = pawnPosition.square, destination = destinationSquare, capturedPiece = pieceOnDestinationSquare)
            .let { move -> if (predicate(move)) return true }
    return false
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

private fun hasPawnDiagonalMovesSatisfying(
        chessboard: Chessboard,
        pawnPosition: PiecePosition,
        diagonalFunction: (Square) -> Square?,
        predicate: (Move) -> Boolean,
): Boolean {
    return diagonalFunction.invoke(pawnPosition.square)?.let { diagonalSquare ->
        chessboard.getPieceAt(diagonalSquare)
                ?.takeIf { it.color == pawnPosition.color.opposite() }
                ?.let { pieceAtDiagonal ->
                    hasOneSquarePawnMovesSatisfying(pawnPosition = pawnPosition, destinationSquare = diagonalSquare, pieceOnDestinationSquare = pieceAtDiagonal, predicate = predicate)
                } ?: false
    } ?: false
}

private fun hasQueenPseudoLegalMovesSatisfying(chessboard: Chessboard, queenPosition: PiecePosition, predicate: (Move) -> Boolean): Boolean {
    return hasBishopPseudoLegalMovesSatisfying(chessboard = chessboard, bishopPosition = queenPosition, predicate = predicate) ||
            hasRookPseudoLegalMovesSatisfying(chessboard = chessboard, rookPosition = queenPosition, predicate = predicate)
}


private fun hasRookPseudoLegalMovesSatisfying(chessboard: Chessboard, rookPosition: PiecePosition, predicate: (Move) -> Boolean): Boolean {
    return listOf(
            { square: Square -> square.left() },
            { square: Square -> square.right() },
            { square: Square -> square.down() },
            { square: Square -> square.up() },
    ).any { squareCreator ->
        hasLegalMoveFollowingDirectionSatisfying(piecePosition = rookPosition, chessboard = chessboard, direction = squareCreator, predicate = predicate)
    }
}

private fun hasBishopPseudoLegalMovesSatisfying(chessboard: Chessboard, bishopPosition: PiecePosition, predicate: (Move) -> Boolean): Boolean {
    return listOf(
            { square: Square -> square.upLeft() },
            { square: Square -> square.upRight() },
            { square: Square -> square.downLeft() },
            { square: Square -> square.downRight() },
    ).any { squareCreator ->
        hasLegalMoveFollowingDirectionSatisfying(piecePosition = bishopPosition, chessboard = chessboard, direction = squareCreator, predicate = predicate)
    }
}

private fun hasKingPseudoLegalMovesSatisfying(game: Game, kingPosition: PiecePosition, predicate: (Move) -> Boolean): Boolean {
    return listOf(
            { kingPosition.square.up() },
            { kingPosition.square.down() },
            { kingPosition.square.left() },
            { kingPosition.square.right() },
            { kingPosition.square.upLeft() },
            { kingPosition.square.upRight() },
            { kingPosition.square.downRight() },
            { kingPosition.square.downLeft() },
    ).any { squareCreator ->
        squareCreator.invoke()?.let {
            if (!game.chessboard.isPiecePresentAtAndHasColor(it, kingPosition.color)) {
                predicate(Move(piece = kingPosition.piece, origin = kingPosition.square, destination = it, capturedPiece = game.chessboard.getPieceAt(it)))
            } else false
        } ?: false
    }.let {
        if (it) return true else hasCastleMovesSatisfying(game, kingPosition, predicate)
    }
}

private fun hasCastleMoveSatisfying(
        game: Game,
        kingPosition: PiecePosition,
        isKingCastle: Boolean,
        isCurrentCastlePossible: Boolean,
        kingDestination: Square,
        kingCastlingPathSquares: List<Square>,
        predicate: (Move) -> Boolean,
): Boolean {
    if (!isCurrentCastlePossible) {
        return false
    }
    if (game.chessboard.hasAtLeastOnePieceAt(kingCastlingPathSquares.minus(kingPosition.square))) {
        return false
    }
    if (
            hasAPseudoLegalMovesSatisfying2(kingPosition.color.opposite(), game.copyWithoutCastling()) {
                kingCastlingPathSquares.contains(it.destination)
            }
    ) {
        if (predicate(Move(piece = kingPosition.piece, origin = kingPosition.square, destination = kingDestination, isKingCastle = isKingCastle, isQueenCastle = !isKingCastle))) {
            return true
        }
    }
    return false
}

private fun Game.copyWithoutCastling() = copy(
        castling = Castling(
                isWhiteKingCastlePossible = false,
                isWhiteQueenCastlePossible = false,
                isBlackQueenCastlePossible = false,
                isBlackKingCastlePossible = false
        ),
)

private fun hasCastleMovesSatisfying(game: Game, kingPosition: PiecePosition, predicate: (Move) -> Boolean): Boolean {

    if (game.sideToMove == Color.WHITE) {
        hasCastleMoveSatisfying(
                game = game,
                kingPosition = kingPosition,
                isKingCastle = true,
                isCurrentCastlePossible = game.castling.isWhiteKingCastlePossible,
                kingDestination = Square.G1,
                kingCastlingPathSquares = listOf(Square.E1, Square.F1, Square.G1),
                predicate = predicate,
        ).let { if (it) return true }
        hasCastleMoveSatisfying(
                game = game,
                kingPosition = kingPosition,
                isKingCastle = false,
                isCurrentCastlePossible = game.castling.isWhiteQueenCastlePossible,
                kingDestination = Square.C1,
                kingCastlingPathSquares = listOf(Square.E1, Square.D1, Square.C1),
                predicate = predicate,
        ).let { if (it) return true }

    } else {
        hasCastleMoveSatisfying(
                game = game,
                kingPosition = kingPosition,
                isKingCastle = true,
                isCurrentCastlePossible = game.castling.isBlackKingCastlePossible,
                kingDestination = Square.G8,
                kingCastlingPathSquares = listOf(Square.E8, Square.F8, Square.G8),
                predicate = predicate,
        ).let { if (it) return true }
        hasCastleMoveSatisfying(
                game = game,
                kingPosition = kingPosition,
                isKingCastle = false,
                isCurrentCastlePossible = game.castling.isBlackQueenCastlePossible,
                kingDestination = Square.C8,
                kingCastlingPathSquares = listOf(Square.E8, Square.D8, Square.C8),
                predicate = predicate,
        ).let { if (it) return true }
    }
    return false
}

private fun hasLegalMoveFollowingDirectionSatisfying(
        piecePosition: PiecePosition,
        currentSquare: Square = piecePosition.square,
        chessboard: Chessboard,
        direction: (origin: Square) -> Square?,
        predicate: (Move) -> Boolean,
): Boolean {
    return direction.invoke(currentSquare)?.let { newSquare ->
        chessboard.getPieceAt(newSquare)?.let { pieceAtNewSquare ->
            if (pieceAtNewSquare.color != piecePosition.piece.color) {
                predicate.invoke(Move(piece = piecePosition.piece, origin = piecePosition.square, destination = newSquare, capturedPiece = pieceAtNewSquare))
            } else
                false
        } ?: (
                if (predicate.invoke(Move(piece = piecePosition.piece, origin = piecePosition.square, destination = newSquare))) {
                    return true
                } else {
                    hasLegalMoveFollowingDirectionSatisfying(piecePosition, newSquare, chessboard, direction, predicate)
                }
                )
    } ?: false
}