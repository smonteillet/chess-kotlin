package fr.smo.chess.server.game

import fr.smo.chess.server.notation.FEN
import fr.smo.chess.server.notation.STARTING_POSITION_FEN

class GameVariants {


    companion object {
        private val fen = FEN()
        fun createStandardGame() = fen.import(STARTING_POSITION_FEN)
    }


}