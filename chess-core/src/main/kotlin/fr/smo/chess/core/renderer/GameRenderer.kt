package fr.smo.chess.core.renderer

import fr.smo.chess.core.File
import fr.smo.chess.core.Piece.*
import fr.smo.chess.core.Position
import fr.smo.chess.core.Rank
import fr.smo.chess.core.Square
import fr.smo.chess.core.notation.FEN.exportFEN
import java.util.*

object GameRenderer {

    fun consoleRender(position: Position): String =
        Rank.entries.reversed().joinToString("\n") { rank ->
            "|" + File.entries.joinToString("|") { file ->
                when (position.chessboard.getPieceAt(Square.at(file, rank)!!)) {
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

    fun consoleRenderV2(position: Position): String {
        val line = "+---+---+---+---+---+---+---+---+"
        return line + "\n" +
                Rank.entries.reversed().joinToString("\n" + line + "\n") { rank ->
                    "|" + File.entries.joinToString("|") { file ->
                        when (position.chessboard.getPieceAt(Square.at(file, rank)!!)) {
                            null -> "   "
                            WHITE_KING -> " K "
                            WHITE_QUEEN -> " Q "
                            WHITE_ROOK -> " R "
                            WHITE_BISHOP -> " B "
                            WHITE_KNIGHT -> " N "
                            WHITE_PAWN -> " P "
                            BLACK_KING -> " k "
                            BLACK_QUEEN -> " q "
                            BLACK_ROOK -> " r "
                            BLACK_BISHOP -> " b "
                            BLACK_KNIGHT -> " n "
                            BLACK_PAWN -> " p "
                        }
                    } + "| " + rank.label
                } + "\n" + line +
                "\n  A   B   C   D   E   F   G   H"
    }

    fun lichessUrlRenderer(position: Position): String {
        val exportedFEN = exportFEN(position)
        return "https://lichess.org/editor/${exportedFEN.replace(" ", "_")}?color=" +
                position.sideToMove.toString().lowercase(Locale.getDefault())
    }

}