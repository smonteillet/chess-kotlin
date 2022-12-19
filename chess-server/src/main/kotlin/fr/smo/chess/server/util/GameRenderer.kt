package fr.smo.chess.server.util

import fr.smo.chess.core.*
import fr.smo.chess.core.Piece.*
import fr.smo.chess.server.notation.FEN
import java.util.*

class GameRenderer {

    companion object {

        fun consoleRender(gameState : GameState): String =
            Rank.values().reversed().joinToString("\n") { rank ->
                "|" + File.values().joinToString("|") { file ->
                    when (gameState.chessboard.getPositionAt(Square.at(file, rank)!!)?.piece) {
                        null -> " "
                        WHITE_KING -> "♔"
                        WHITE_QUEEN -> "♕"
                        WHITE_ROOK -> "♖"
                        WHITE_BISHOP -> "♗"
                        WHITE_KNIGHT -> "♘"
                        WHITE_PAWN -> "♙"
                        BLACK_KING -> "♚"
                        BLACK_QUEEN -> "♛"
                        BLACK_ROOK -> "♜"
                        BLACK_BISHOP -> "♝"
                        BLACK_KNIGHT -> "♞"
                        BLACK_PAWN -> "♟"
                    }
                } + "| " + rank.label
            } + "\n A B C D E F G H"

        fun lichessUrlRenderer(gameState : GameState): String {
            val exportedFEN = FEN().export(gameState)
            return "https://lichess.org/editor/${exportedFEN.replace(" ", "_")}?color=" +
                    gameState.sideToMove.toString().lowercase(Locale.getDefault())
        }
    }
}