package fr.smo.chess.core

data class Chessboard(
    val piecesOnBoard: List<Position> = listOf(),
) {

    fun isPiecePresentAtAndHasColor(square: Square, color: Color) : Boolean = getPositionAt(square)?.piece?.color == color

    fun getPositionAt(square: Square): Position? {
        return this.piecesOnBoard.firstOrNull { it.square == square }
    }

}
