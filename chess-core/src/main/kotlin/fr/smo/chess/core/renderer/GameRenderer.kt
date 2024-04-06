package fr.smo.chess.core.renderer

import fr.smo.chess.core.File
import fr.smo.chess.core.Game
import fr.smo.chess.core.Piece.*
import fr.smo.chess.core.Rank
import fr.smo.chess.core.Square
import fr.smo.chess.core.notation.FEN.exportFEN
import java.util.*

object GameRenderer {

    fun consoleRender(game: Game): String =
        Rank.entries.reversed().joinToString("\n") { rank ->
            "|" + File.entries.joinToString("|") { file ->
                when (game.chessboard.getPieceAt(Square.at(file, rank)!!)) {
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
        } + "\n|A B C D E F G H"

    fun lichessUrlRenderer(game: Game): String {
        val exportedFEN = exportFEN(game)
        return "https://lichess.org/editor/${exportedFEN.replace(" ", "_")}?color=" +
                game.sideToMove.toString().lowercase(Locale.getDefault())
    }

}