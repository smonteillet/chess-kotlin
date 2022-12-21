package fr.smo.chess.core.notation

import fr.smo.chess.core.Game
import fr.smo.chess.core.Move
import fr.smo.chess.core.PieceType.*

object PGN {

    fun exportPGN(game: Game): String {
        val pgnHistory = game.history.moves.mapIndexed { index, move ->
            val indexStr = if (index % 2 == 0) "${index / 2 + 1}. " else ""
            return@mapIndexed indexStr + moveToPgn(move)
        }.joinToString(" ")
        val pgnOutcome = when (game.status) {
            Game.Status.BLACK_WIN -> "0-1"
            Game.Status.WHITE_WIN -> "1-0"
            Game.Status.DRAW -> "1/2-1/2"
            else -> ""
        }
        return pgnHistory + pgnOutcome
    }

    private fun moveToPgn(move: Move): String {
        return if (move.isKingCastle) {
            "O-O"
        } else if (move.isQueenCastle) {
            "O-O-O"
        } else {
            val piece = when (move.piece.type) {
                PAWN -> "" + move.from
                BISHOP -> "B"
                KNIGHT -> "N" + move.from
                QUEEN -> "Q"
                KING -> "K"
                ROOK -> "R" + move.from
            }
            val captured = if (move.capturedPiece != null) "x" else ""
            val destination = move.destination
            val promotedTo = if (move.promotedTo != null) {
                when (move.promotedTo.type) {
                    BISHOP -> "=B"
                    KNIGHT -> "=N"
                    QUEEN -> "=Q"
                    ROOK -> "=R"
                    else -> throw IllegalStateException("cannot promote to ${move.promotedTo}")
                }
            } else {
                ""
            }
            return piece + captured + destination + promotedTo
        }
    }
}

