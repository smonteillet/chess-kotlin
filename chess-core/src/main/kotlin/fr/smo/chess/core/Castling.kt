package fr.smo.chess.core

data class Castles(val castles : List<Castle>) {
    fun updateCastlesAfterMove(move: Move): Castles {
        return Castles(
            castles = castles.map{ it.updateAfterMove(move) }
        )
    }

    val isBlackKingCastlePossible : Boolean
        get() = castles
            .filter { it.color == Color.BLACK }
            .firstOrNull { it.castleType == CastleType.SHORT }
            ?.isCastleStillPossible ?: false

    val isBlackQueenCastlePossible : Boolean
        get() = castles
            .filter { it.color == Color.BLACK }
            .firstOrNull { it.castleType == CastleType.LONG }
            ?.isCastleStillPossible ?: false

    val isWhiteKingCastlePossible : Boolean
        get() = castles
            .filter { it.color == Color.WHITE }
            .firstOrNull { it.castleType == CastleType.SHORT }
            ?.isCastleStillPossible ?: false

    val isWhiteQueenCastlePossible : Boolean
        get() = castles
            .filter { it.color == Color.WHITE }
            .firstOrNull { it.castleType == CastleType.LONG }
            ?.isCastleStillPossible ?: false

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
    val kingDestinationSquare: Square =  when (color) {
        Color.WHITE -> if (castleType == CastleType.LONG) Square.C1 else Square.G1
        Color.BLACK -> if (castleType == CastleType.LONG) Square.C8 else Square.G8
    },
    val rookDestinationSquare: Square =  when (color) {
        Color.WHITE -> if (castleType == CastleType.LONG) Square.D1 else Square.F1
        Color.BLACK -> if (castleType == CastleType.LONG) Square.D8 else Square.F8
    },
    val kingCastlingPathSquares : List<Square> = when (color) {
        Color.WHITE -> if (castleType == CastleType.SHORT) listOf(Square.E1, Square.F1, Square.G1) else listOf(Square.E1, Square.D1, Square.C1)
        Color.BLACK -> if (castleType == CastleType.SHORT) listOf(Square.E8, Square.F8, Square.G8) else listOf(Square.E8, Square.D8, Square.C8)
    },
    val squaresBetweenKingAndRook : List<Square> = when (color) {
        Color.WHITE -> if (castleType == CastleType.SHORT) listOf(Square.F1, Square.G1) else listOf(Square.B1, Square.C1, Square.D1)
        Color.BLACK -> if (castleType == CastleType.SHORT) listOf(Square.F8, Square.G8) else listOf(Square.B8, Square.C8, Square.D8)
    },
) {

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