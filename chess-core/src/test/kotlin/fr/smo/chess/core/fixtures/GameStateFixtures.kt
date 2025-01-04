package fr.smo.chess.core.fixtures

import fr.smo.chess.core.*
import fr.smo.chess.core.notation.FEN.getChessboardFromFenPiecePlacement
import fr.smo.chess.core.notation.FEN.importFEN
import fr.smo.chess.core.notation.PGN.import
import fr.smo.chess.core.notation.STARTING_POSITION_FEN
import fr.smo.chess.core.renderer.GameRenderer
import fr.smo.chess.core.variant.Standard
import fr.smo.chess.core.variant.Variant

object GameStateFixtures {


    fun givenAChessGame(fullFEN: String = STARTING_POSITION_FEN): Position {
        return importFEN(fullFEN).apply { render(this) }
    }

    fun givenAChessGameWithHistory(pgn : String, variant: Variant = Standard) : Position {
        return import(pgn, variant)
    }

    fun givenAChessGame(
        fenPiecePlacementOnly: String = STARTING_POSITION_FEN,
        sideToMove: Color = Color.WHITE,
        isWhiteKingCastlePossible: Boolean = false,
        isWhiteQueenCastlePossible: Boolean = false,
        isBlackKingCastlePossible: Boolean = false,
        isBlackQueenCastlePossible: Boolean = false,
        enPassantTargetSquare: Square? = null,
        history: History = History(),
        variant: Variant = Standard,
    ): Position {
        return Position(
            chessboard = getChessboardFromFenPiecePlacement(fenPiecePlacementOnly),
            history = history,
            sideToMove = sideToMove,
            castling = Castling(
                isWhiteKingCastlePossible = isWhiteKingCastlePossible,
                isWhiteQueenCastlePossible = isWhiteQueenCastlePossible,
                isBlackKingCastlePossible = isBlackKingCastlePossible,
                isBlackQueenCastlePossible = isBlackQueenCastlePossible,
            ),
            enPassantTargetSquare = enPassantTargetSquare,
            variant = variant,
        ).apply { render(this) }
    }

    private fun render(position: Position) {
        println(GameRenderer.lichessUrlRenderer(position))
        println(GameRenderer.consoleRenderV2(position))
    }


}