package fr.smo.chess.service

import fr.smo.chess.model.GameState
import fr.smo.chess.model.Move
import fr.smo.chess.model.PieceType.*

class PGN {

    fun export(gameState: GameState): String {
        var pgn = ""
        gameState.moveHistory.forEachIndexed { index, move ->
            if (index % 2 == 0) {
                val moveIndex = index / 2 + 1
                pgn += "$moveIndex. "
            }
            pgn += moveToPgn(move) + " "
        }
        pgn += when (gameState.gameOutcome) {
            GameState.GameOutcome.BLACK_WIN -> "0-1"
            GameState.GameOutcome.WHITE_WIN -> "1-0"
            GameState.GameOutcome.DRAW -> "1/2-1/2"
            GameState.GameOutcome.NOT_FINISHED_YET -> ""
        }
        return pgn
    }

    private fun moveToPgn(move: Move): String {
        return if (move.isKingCastle) {
            "O-O"
        } else if (move.isQueenCastle) {
            "O-O-O"
        } else {
            var movePGN = when (move.piece.type) {
                PAWN -> "" + move.from
                BISHOP -> "B"
                KNIGHT -> "N" + move.from
                QUEEN -> "Q"
                KING -> "K"
                ROOK -> "R" + move.from
            }
            if (move.capturedPiece != null) {
                movePGN = movePGN.plus("x")
            }
            movePGN = movePGN.plus(move.destination)
            if (move.promotedTo != null) {
                movePGN = when (move.promotedTo.type) {
                    BISHOP -> "$movePGN=B"
                    KNIGHT -> "$movePGN=N"
                    QUEEN -> "$movePGN=Q"
                    ROOK -> "$movePGN=R"
                    else -> throw IllegalStateException("cannot promote to ${move.promotedTo}")
                }
            }
            movePGN
        }
    }


}