package fr.smo.chess.core

import fr.smo.chess.core.Square.*

data class Castles(val castles : List<Castle>) {
    fun updateCastlesAfterMove(move: Move): Castles {
        return Castles(
            castles = castles.map{ it.updateAfterMove(move) }
        )
    }

    val isBlackKingCastlePossible : Boolean
        get() = getCastle(Color.BLACK, CastleType.SHORT).isCastleStillPossible

    val isBlackQueenCastlePossible : Boolean
        get() = getCastle(Color.BLACK, CastleType.LONG).isCastleStillPossible

    val isWhiteKingCastlePossible : Boolean
        get() = getCastle(Color.WHITE, CastleType.SHORT).isCastleStillPossible

    val isWhiteQueenCastlePossible : Boolean
        get() = getCastle(Color.WHITE, CastleType.LONG).isCastleStillPossible

    fun getCastle(color : Color, castleType: CastleType) : Castle {
        return castles
            .filter { it.color == color }
            .first{ it.castleType == castleType }
    }
}

data class Castle(
    val color: Color,
    val castleType: CastleType,
    val kingStartSquare: Square,
    val rookStartSquare: Square,
    val isCastleStillPossible: Boolean = true,
    val kingDestinationSquare: Square = when (color) {
        Color.WHITE -> if (castleType == CastleType.LONG) C1 else G1
        Color.BLACK -> if (castleType == CastleType.LONG) C8 else G8
    },
    val rookDestinationSquare: Square = when (color) {
        Color.WHITE -> if (castleType == CastleType.LONG) D1 else F1
        Color.BLACK -> if (castleType == CastleType.LONG) D8 else F8
    },
    val kingCastlingPathSquares: List<Square> = kingStartSquare.getSquareBetweenIncludingBounds(kingDestinationSquare),
    val rookCastlingPathSquares: List<Square> = rookStartSquare.getSquareBetweenIncludingBounds(rookDestinationSquare)
) {

    val kingPiece
        get() = when (color) {
            Color.WHITE -> Piece.WHITE_KING
            Color.BLACK -> Piece.BLACK_KING
        }

    fun updateAfterMove(move : Move) : Castle {
        val myKingHasMoved = move.piece.type == PieceType.KING && move.piece.color == color
        val myRookHasMovedOrCaptured = move.origin == rookStartSquare || move.destination == rookStartSquare
        return copy(isCastleStillPossible = isCastleStillPossible && !myKingHasMoved && !myRookHasMovedOrCaptured)
    }

    fun getFenLetter()  : String {
        return when (color) {
            Color.WHITE -> if (castleType == CastleType.SHORT) "K" else "Q"
            Color.BLACK -> if (castleType == CastleType.SHORT) "k" else "q"
        }
    }
}

enum class CastleType { LONG, SHORT }