package fr.smo.chess.core

import fr.smo.chess.core.notation.STARTING_POSITION_FEN
import fr.smo.chess.core.notation.importFEN

class GameFactory {


    companion object {
        fun createStandardGame() = importFEN(STARTING_POSITION_FEN)
    }


}