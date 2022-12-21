package fr.smo.chess.core

import fr.smo.chess.core.notation.FEN.importFEN
import fr.smo.chess.core.notation.STARTING_POSITION_FEN

object GameFactory {

    fun createStandardGame() = importFEN(STARTING_POSITION_FEN)

}