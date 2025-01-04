package fr.smo.chess.core

import fr.smo.chess.core.notation.FEN.importFEN
import fr.smo.chess.core.notation.STARTING_STANDARD_POSITION_FEN
import fr.smo.chess.core.variant.Standard
import fr.smo.chess.core.variant.Variant

object GameFactory {

    fun createStandardGame() = importFEN(STARTING_STANDARD_POSITION_FEN, Standard)

    fun createGame(variant: Variant) : Position = importFEN(variant.getStartingFenPosition(), variant)

}