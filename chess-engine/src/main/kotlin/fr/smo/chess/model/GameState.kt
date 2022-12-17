package fr.smo.chess.model

import fr.smo.chess.model.Color.BLACK
import fr.smo.chess.model.Color.WHITE
import fr.smo.chess.service.FEN
import fr.smo.chess.service.STARTING_POSITION_FEN

data class GameState(
    val chessboard: Chessboard,
    val moveHistory: List<Move> = listOf(),
    val sideToMove: Color = WHITE,
    val isWhiteKingCastlePossible : Boolean = true,
    val isWhiteQueenCastlePossible : Boolean = true,
    val isBlackKingCastlePossible : Boolean = true,
    val isBlackQueenCastlePossible : Boolean = true,
    val enPassantTargetSquare : Square? = null,
    val gameOutcome : GameOutcome = GameOutcome.NOT_FINISHED_YET,
    val fullMoveCounter : Int = 1,
    val halfMoveClock : Int = 0,
) {

    companion object StartingPositions {
        private val FEN = FEN()
        val NEW_STANDARD_CHESS_GAME = FEN.import(STARTING_POSITION_FEN)
    }

    val isNextPlayCouldBeWhiteCastle: Boolean =
        sideToMove == WHITE && isWhiteKingCastlePossible && isWhiteQueenCastlePossible
    val  isNextPlayCouldBeBlackCastle: Boolean =
        sideToMove == BLACK && isBlackKingCastlePossible && isBlackQueenCastlePossible

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
