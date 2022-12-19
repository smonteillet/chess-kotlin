package fr.smo.chess.server.fixtures

import fr.smo.chess.core.Color
import fr.smo.chess.core.GameState
import fr.smo.chess.core.Move
import fr.smo.chess.core.Square
import fr.smo.chess.server.notation.STARTING_POSITION_FEN
import fr.smo.chess.server.notation.FEN
import fr.smo.chess.server.util.GameRenderer

class GameStateFixtures {

    companion object {
        private val FEN = FEN()


        fun givenAChessGame(fullFEN: String = STARTING_POSITION_FEN): GameState {
            return FEN.import(fullFEN).apply { render(this) }
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
            ).apply { render(this) }
        }

        private fun render(gameState: GameState) {
            println(GameRenderer.lichessUrlRenderer(gameState))
            println(GameRenderer.consoleRender(gameState))
        }
    }

}