package fr.smo.chess.core.fixtures

import fr.smo.chess.core.*
import fr.smo.chess.core.notation.FEN.getChessboardFromFenPiecePlacement
import fr.smo.chess.core.notation.FEN.importFEN
import fr.smo.chess.core.notation.PGN.importPGN
import fr.smo.chess.core.notation.STARTING_STANDARD_POSITION_FEN
import fr.smo.chess.core.renderer.GameRenderer
import fr.smo.chess.core.variant.Chess960
import fr.smo.chess.core.variant.Standard
import fr.smo.chess.core.variant.Variant

object GameStateFixtures {


    fun givenAChessGame(fullFEN: String = STARTING_STANDARD_POSITION_FEN): Position {
        return importFEN(fullFEN).apply { render(this) }
    }

    fun givenAChess960Game(startingPosition : String, pgn: String): Position {
        return importPGN(pgn, Chess960, startingPosition).apply { render(this) }
    }

    fun givenAChessGameWithHistory(pgn: String, variant: Variant = Standard): Position {
        return importPGN(pgn, variant)
    }

    fun givenAChessGame(
        fenPiecePlacementOnly: String = STARTING_STANDARD_POSITION_FEN,
        sideToMove: Color = Color.WHITE,
        isWhiteKingCastlePossible: Boolean = false,
        isWhiteQueenCastlePossible: Boolean = false,
        isBlackKingCastlePossible: Boolean = false,
        isBlackQueenCastlePossible: Boolean = false,
        enPassantTargetSquare: Square? = null,
        history: History = History(),
        variant: Variant = Standard,
    ): Position {
        val chessboard = getChessboardFromFenPiecePlacement(fenPiecePlacementOnly)
        val castles = variant.initCastles(chessboard)
        return Position(
            chessboard = chessboard,
            history = history,
            sideToMove = sideToMove,
            castles = castles.copy(castles = castles.castles.map {
                // FIXME there should be a better way to do this
                if (it.castleType == CastleType.SHORT && it.color == Color.WHITE && !isWhiteKingCastlePossible) {
                    it.copy(isCastleStillPossible = false)
                } else if (it.castleType == CastleType.LONG && it.color == Color.WHITE && !isWhiteQueenCastlePossible) {
                    it.copy(isCastleStillPossible = false)
                } else if (it.castleType == CastleType.SHORT && it.color == Color.BLACK && !isBlackKingCastlePossible) {
                    it.copy(isCastleStillPossible = false)
                } else if (it.castleType == CastleType.LONG && it.color == Color.BLACK && !isBlackQueenCastlePossible) {
                    it.copy(isCastleStillPossible = false)
                }  else {
                    it
                }
            }),
            enPassantTargetSquare = enPassantTargetSquare,
            variant = variant,
        ).apply { render(this) }
    }

    private fun render(position: Position) {
        println(GameRenderer.lichessUrlRenderer(position))
        println(GameRenderer.consoleRenderV2(position))
    }


}