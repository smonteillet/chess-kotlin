package fr.smo.chess.core.fixtures

import fr.smo.chess.core.Color
import fr.smo.chess.core.Game
import fr.smo.chess.core.Move
import fr.smo.chess.core.Square
import fr.smo.chess.core.notation.STARTING_POSITION_FEN
import fr.smo.chess.core.notation.getChessboardFromFenPiecePlacement
import fr.smo.chess.core.notation.importFEN
import fr.smo.chess.core.renderer.GameRenderer

class GameStateFixtures {

    companion object {

        fun givenAChessGame(fullFEN: String = STARTING_POSITION_FEN): Game {
            return importFEN(fullFEN).apply { render(this) }
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
            status: Game.Status = Game.Status.IN_PROGRESS,
            halfMoveClock: Int = 0,
            fullMoveCounter: Int = 1,
        ): Game {
            return Game(
                chessboard = getChessboardFromFenPiecePlacement(fenPiecePlacementOnly),
                moveHistory = moveHistory,
                sideToMove = sideToMove,
                isWhiteKingCastlePossible = isWhiteKingCastlePossible,
                isWhiteQueenCastlePossible = isWhiteQueenCastlePossible,
                isBlackKingCastlePossible = isBlackKingCastlePossible,
                isBlackQueenCastlePossible = isBlackQueenCastlePossible,
                enPassantTargetSquare = enPassantTargetSquare,
                status = status,
                halfMoveClock = halfMoveClock,
                fullMoveCounter = fullMoveCounter,
            ).apply { render(this) }
        }

        private fun render(game: Game) {
            println(GameRenderer.lichessUrlRenderer(game))
            println(GameRenderer.consoleRender(game))
        }
    }

}