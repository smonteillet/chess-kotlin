package fr.smo.chess.service

import fr.smo.chess.model.GameState
import fr.smo.chess.model.Move
import fr.smo.chess.model.PieceType.*

class PGN {

    fun export(gameState: GameState): String {
        val pgnHistory = gameState.moveHistory.mapIndexed { index, move ->
            val indexStr = if (index % 2 == 0) "${index / 2 + 1}. " else ""
            return@mapIndexed indexStr + moveToPgn(move)
        }.joinToString(" ")
        val pgnOutcome = when (gameState.gameOutcome) {
            GameState.GameOutcome.BLACK_WIN -> "0-1"
            GameState.GameOutcome.WHITE_WIN -> "1-0"
            GameState.GameOutcome.DRAW -> "1/2-1/2"
            GameState.GameOutcome.NOT_FINISHED_YET -> ""
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
            val promotedTo =  if (move.promotedTo != null) {
               when (move.promotedTo.type) {
                    BISHOP -> "=B"
                    KNIGHT -> "=N"
                    QUEEN -> "=Q"
                    ROOK -> "=R"
                    else -> throw IllegalStateException("cannot promote to ${move.promotedTo}")
                }
            }
            else{
                ""
            }
            return piece + captured + destination + promotedTo
        }
    }


}