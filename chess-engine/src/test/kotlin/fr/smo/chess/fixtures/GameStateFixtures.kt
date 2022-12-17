package fr.smo.chess.fixtures

import fr.smo.chess.model.Color
import fr.smo.chess.model.GameState
import fr.smo.chess.model.Move
import fr.smo.chess.model.Square
import fr.smo.chess.service.FEN
import fr.smo.chess.service.STARTING_POSITION_FEN
import java.util.*

class GameStateFixtures {

    companion object {
        private val FEN = FEN()


        fun givenAChessGame(fullFEN: String = STARTING_POSITION_FEN): GameState {
            return FEN.import(fullFEN).apply {
                logLichessEditorUrl(this)
            }
        }

        fun givenAChessGame(
            fenPiecePlacementOnly: String = STARTING_POSITION_FEN,
            moveHistory: List<Move> = listOf(),
            sideToMove: Color = Color.WHITE,
            isWhiteKingCastlePossible: Boolean = false,
            isWhiteQueenCastlePossible: Boolean = false,
            isBlackKingCastlePossible: Boolean = false,
            isBlackQueenCastlePossible: Boolean = false,
            enPassantTargetSquare: Square? = null,
            gameOutcome: GameState.GameOutcome = GameState.GameOutcome.NOT_FINISHED_YET,
            halfMoveClock: Int = 0,
            fullMoveCounter: Int = 1,
        ): GameState {
            return GameState(
                chessboard = FEN.getChessboardFromFenPiecePlacement(fenPiecePlacementOnly),
                moveHistory = moveHistory,
                sideToMove = sideToMove,
                isWhiteKingCastlePossible = isWhiteKingCastlePossible,
                isWhiteQueenCastlePossible = isWhiteQueenCastlePossible,
                isBlackKingCastlePossible = isBlackKingCastlePossible,
                isBlackQueenCastlePossible = isBlackQueenCastlePossible,
                enPassantTargetSquare = enPassantTargetSquare,
                gameOutcome = gameOutcome,
                halfMoveClock = halfMoveClock,
                fullMoveCounter = fullMoveCounter,
            ).apply {
                logLichessEditorUrl(this)
            }
        }

        private fun logLichessEditorUrl(gameState: GameState) {
            val exportedFEN = FEN.export(gameState)
            println("https://lichess.org/editor/${exportedFEN.replace(" ", "_")}?color=" +
                    gameState.sideToMove.toString().lowercase(Locale.getDefault())
            )
        }
    }

}