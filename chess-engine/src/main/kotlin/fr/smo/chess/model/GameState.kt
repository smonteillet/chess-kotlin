package fr.smo.chess.model

data class GameState(
    val chessboard: Chessboard = Chessboard.initNewBoard(),
    val moveHistory: List<Move> = listOf(),
    val sideToMove: Color = Color.WHITE,
    val isWhiteKingCastlePossible : Boolean = true,
    val isWhiteQueenCastlePossible : Boolean = true,
    val isBlackKingCastlePossible : Boolean = true,
    val isBlackQueenCastlePossible : Boolean = true,
    val enPassantTargetSquare : Square? = null,
    val gameOutcome : GameOutcome = GameOutcome.NOT_FINISHED_YET,
    val fullMoveCounter : Int = 1,
    val halfMoveClock : Int = 0,
) {

    enum class GameOutcome {
        BLACK_WIN,
        WHITE_WIN,
        DRAW,
        NOT_FINISHED_YET
    }

    fun withoutCastleAndEnPassant(): GameState {
        return this.copy(
            isWhiteKingCastlePossible = false,
            isWhiteQueenCastlePossible = false,
            isBlackQueenCastlePossible = false,
            isBlackKingCastlePossible = false,
            enPassantTargetSquare = null,
        )
    }

}
