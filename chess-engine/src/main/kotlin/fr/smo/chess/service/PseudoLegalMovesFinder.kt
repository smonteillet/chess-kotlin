package fr.smo.chess.service

import fr.smo.chess.model.Chessboard.Position
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

    fun getAllPseudoLegalMoves(fromPosition : Position, gameState: GameState): List<Move> {
        return when (fromPosition.piece.type) {
            PAWN ->   getPawnPseudoLegalMoves(fromPosition, gameState)
            BISHOP -> getBishopPseudoLegalMoves(fromPosition, gameState.chessboard)
            KNIGHT -> getKnightPseudoLegalMoves(fromPosition, gameState.chessboard)
            QUEEN ->  getQueenPseudoLegalMoves(fromPosition, gameState.chessboard)
            KING ->   getKingPseudoLegalMoves(fromPosition, gameState)
            ROOK ->   getRookPseudoLegalMoves(fromPosition, gameState.chessboard)
        }
    }

    fun getAllPseudoLegalMovesForPlayer(color : Color, gameState: GameState): List<Move> {
        return gameState.chessboard.piecesOnBoard
            .filter { it.piece.color == color }
            .flatMap {
                getAllPseudoLegalMoves(it, gameState)
            }
    }

    private fun getKnightPseudoLegalMoves(knightPosition : Position, chessboard: Chessboard): List<Move> {
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


    private fun getPawnPseudoLegalMoves(pawnPosition : Position, gameState: GameState): List<Move> {
        val moves = mutableListOf<Move>()
        if (pawnPosition.piece.color == WHITE) {
            getPawnOneSquareAndTwoSquaresMoves(
                chessboard = gameState.chessboard,
                pawnPosition = pawnPosition,
                hasPawnNotAlreadyMoved = pawnPosition.square.row == 2,
                pawnFrontMoveFunction = { it.up() },
            ).let { moves.addAll(it) }
            getPawnDiagonalMoves(gameState.chessboard, pawnPosition) { it.upRight() }?.let { moves.add(it) }
            getPawnDiagonalMoves(gameState.chessboard, pawnPosition) { it.upLeft() }?.let { moves.add(it) }
        }
        if (pawnPosition.piece.color == BLACK) {
            getPawnOneSquareAndTwoSquaresMoves(
                chessboard = gameState.chessboard,
                pawnPosition = pawnPosition,
                hasPawnNotAlreadyMoved = pawnPosition.square.row == 7,
                pawnFrontMoveFunction = { it.down() },
            ).let { moves.addAll(it) }
            getPawnDiagonalMoves(gameState.chessboard, pawnPosition) { it.downRight() }?.let { moves.add(it) }
            getPawnDiagonalMoves(gameState.chessboard, pawnPosition) { it.downLeft() }?.let { moves.add(it) }
        }
        getPawnEnPassantMove(pawnPosition, gameState)?.let { moves.add(it) }
        return moves
    }

    private fun getPawnEnPassantMove(pawnPosition: Position, gameState: GameState) : Move? {
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
            ){
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
        val oneSquareMove = pawnFrontMoveFunction.invoke(pawnPosition.square)
        val moves = mutableListOf<Move>()
        if (oneSquareMove != null && chessboard.getPositionAt(oneSquareMove) == null) {
            val promotedPiecesMoves = getPromotedPieces(pawnPosition.piece.color, oneSquareMove)
            if (promotedPiecesMoves.isEmpty()) {
                moves.add(
                    Move(
                        piece = pawnPosition.piece,
                        from = pawnPosition.square,
                        destination = oneSquareMove,
                    )
                )
            } else {
                moves.addAll(promotedPiecesMoves.map {
                    Move(
                        piece = pawnPosition.piece,
                        from = pawnPosition.square,
                        destination = oneSquareMove,
                        promotedTo = it
                    )
                })
            }
            if (hasPawnNotAlreadyMoved) {
                val twoSquaresMove = pawnFrontMoveFunction.invoke(oneSquareMove)
                if (twoSquaresMove != null && chessboard.getPositionAt(twoSquaresMove) == null) {
                    moves.add(
                        Move(
                            piece = pawnPosition.piece,
                            from = pawnPosition.square,
                            destination = twoSquaresMove,
                        )
                    )
                }
            }
        }
        return moves
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

    private fun getQueenPseudoLegalMoves(queenPosition: Position, chessboard: Chessboard): List<Move> {
        return  getLegalMovesFollowingDirection(queenPosition, chessboard) { it.up() } +
                getLegalMovesFollowingDirection(queenPosition, chessboard) { it.down() } +
                getLegalMovesFollowingDirection(queenPosition, chessboard) { it.left() } +
                getLegalMovesFollowingDirection(queenPosition, chessboard) { it.right() } +
                getLegalMovesFollowingDirection(queenPosition, chessboard) { it.upLeft() } +
                getLegalMovesFollowingDirection(queenPosition, chessboard) { it.upRight() } +
                getLegalMovesFollowingDirection(queenPosition, chessboard) { it.downLeft() } +
                getLegalMovesFollowingDirection(queenPosition, chessboard) { it.downRight() }

    }

    private fun getRookPseudoLegalMoves(rookPosition: Position, chessboard: Chessboard): List<Move> {
        return  getLegalMovesFollowingDirection(rookPosition, chessboard) { it.up() } +
                getLegalMovesFollowingDirection(rookPosition, chessboard) { it.down() } +
                getLegalMovesFollowingDirection(rookPosition, chessboard) { it.left() } +
                getLegalMovesFollowingDirection(rookPosition, chessboard) { it.right() }
    }

    private fun getBishopPseudoLegalMoves(bishopPosition : Position, chessboard: Chessboard): List<Move> {
        return  getLegalMovesFollowingDirection(bishopPosition, chessboard) { it.upLeft() } +
                getLegalMovesFollowingDirection(bishopPosition, chessboard) { it.upRight() } +
                getLegalMovesFollowingDirection(bishopPosition, chessboard) { it.downLeft() } +
                getLegalMovesFollowingDirection(bishopPosition, chessboard) { it.downRight() }
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
    ): MutableList<Move> {
        val castleMoves = mutableListOf<Move>()
        val isTherePiecesOnQueenCastlingPath = gameState.chessboard.piecesOnBoard
            .any {queenCastlingPathSquares.minus(kingPosition.square).contains(it.square)  }
        val isTherePiecesOnKingCastlingPath = gameState.chessboard.piecesOnBoard
            .any { kingCastlingPathSquares.minus(kingPosition.square).contains(it.square) }
        val gameStateWithoutCastling = gameState.copy(
            isWhiteKingCastlePossible = false, isWhiteQueenCastlePossible = false, isBlackQueenCastlePossible = false,
            isBlackKingCastlePossible = false, enPassantTargetSquare = null,
        )
        val hasKingCastleSquaresUnderAttack: Boolean = getAllPseudoLegalMovesForPlayer(kingPosition.piece.color.opposite(), gameStateWithoutCastling)
                .any { kingCastlingPathSquares.contains(it.destination) }
        val hasQueenCastleSquaresUnderAttack: Boolean = getAllPseudoLegalMovesForPlayer(kingPosition.piece.color.opposite(), gameStateWithoutCastling)
                .any { queenCastlingPathSquares.contains(it.destination) }
        if (isKingCastlePossible && !isTherePiecesOnKingCastlingPath && !hasKingCastleSquaresUnderAttack) {
            castleMoves.add(
                Move(
                    piece = kingPosition.piece,
                    from = kingPosition.square,
                    destination = kingDestinationKingCastle,
                    isKingCastle = true,
                )
            )
        }
        if (isQueenCastlePossible && !isTherePiecesOnQueenCastlingPath && !hasQueenCastleSquaresUnderAttack) {
            castleMoves.add(
                Move(
                    piece = kingPosition.piece,
                    from = kingPosition.square,
                    destination = kingDestinationQueenCastle,
                    isQueenCastle = true,
                )
            )
        }
        return castleMoves

    }

    private fun getCastleMoveIfPossible(kingPosition: Position, gameState: GameState): List<Move> {
        val castleMoves = mutableListOf<Move>()
        val castleHasAlreadyBeenPerformed =
            (kingPosition.piece.color == WHITE && !gameState.isWhiteKingCastlePossible && !gameState.isWhiteQueenCastlePossible) ||
            (kingPosition.piece.color == BLACK && !gameState.isBlackQueenCastlePossible && !gameState.isBlackKingCastlePossible)
        if (castleHasAlreadyBeenPerformed) {
            return emptyList()
        }
        if (kingPosition.piece.color == WHITE) {
            getCastleMoves(
                gameState = gameState,
                kingPosition = kingPosition,
                isKingCastlePossible = gameState.isWhiteKingCastlePossible,
                isQueenCastlePossible = gameState.isWhiteQueenCastlePossible,
                kingDestinationKingCastle = square("g1"),
                kingDestinationQueenCastle = square("c1"),
                kingCastlingPathSquares = listOf(square("e1"), square("f1"), square("g1")),
                queenCastlingPathSquares = listOf(square("e1"), square("d1"), square("c1")),
            ).let { castleMoves.addAll(it) }
        } else if (kingPosition.piece.color == BLACK) {
            getCastleMoves(
                gameState = gameState,
                kingPosition = kingPosition,
                isKingCastlePossible = gameState.isBlackKingCastlePossible,
                isQueenCastlePossible = gameState.isBlackQueenCastlePossible,
                kingDestinationKingCastle = square("g8"),
                kingDestinationQueenCastle = square("c8"),
                kingCastlingPathSquares = listOf(square("e8"), square("f8"), square("g8")),
                queenCastlingPathSquares = listOf(square("e8"), square("d8"), square("c8")),
            ).let { castleMoves.addAll(it) }
        }
        return castleMoves
    }

    private fun getLegalMovesFollowingDirection(
        fromPosition: Position,
        chessboard: Chessboard,
        direction: (from: Square) -> Square?
    ): List<Move> {
        var currentSquare = direction.invoke(fromPosition.square)
        val legalMoves = mutableListOf<Move>()
        while (currentSquare != null) {
            val newPosition = chessboard.getPositionAt(currentSquare)
            if (newPosition == null) {
                legalMoves.add(
                    Move(
                        piece = fromPosition.piece,
                        from = fromPosition.square,
                        destination = currentSquare,
                    )
                )
            } else if (newPosition.piece.color != fromPosition.piece.color) {
                legalMoves.add(
                    Move(
                        piece = fromPosition.piece,
                        from = fromPosition.square,
                        destination = currentSquare,
                        capturedPiece = newPosition.piece,
                    )
                )
                return legalMoves
            }
            else {
                return legalMoves
            }
            currentSquare = direction.invoke(currentSquare)
        }
        return legalMoves
    }
}