package fr.smo.chess.core.notation

import fr.smo.chess.core.*
import fr.smo.chess.core.Color.BLACK
import fr.smo.chess.core.Color.WHITE
import fr.smo.chess.core.Piece.*
import java.util.*

const val STARTING_POSITION_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

object FEN {
    fun importFEN(fen: String): Game {
        val fenSplit = fen.split(" ")
        require(fenSplit.size == 6)
        return Game(
            chessboard = getChessboardFromFenPiecePlacement(fenSplit[0]),
            sideToMove = sideToMove(fenSplit[1]),
            castling = Castling(
                isBlackKingCastlePossible = fenSplit[2].contains("k"),
                isBlackQueenCastlePossible = fenSplit[2].contains("q"),
                isWhiteQueenCastlePossible = fenSplit[2].contains("Q"),
                isWhiteKingCastlePossible = fenSplit[2].contains("K"),
            ),
            enPassantTargetSquare = enPassantTargetSquare(fenSplit[3]),
            history = History(
                halfMoveClock = fenSplit[4].toInt(),
                fullMoveCounter = fenSplit[5].toInt(),
            )
        )
    }

    fun exportFEN(game: Game): String {
        return getFenPiecePlacementFromGameState(game) + " " +
                getFenSideToMoveFromGameState(game) + " " +
                getFenCastlingFromGameState(game) + " " +
                (game.enPassantTargetSquare?.toString() ?: "-") + " " +
                game.history.halfMoveClock + " " +
                game.history.fullMoveCounter
    }

    private fun getFenSideToMoveFromGameState(game: Game): String {
        return if (game.sideToMove == WHITE) {
            "w"
        } else {
            "b"
        }
    }

    private fun getFenCastlingFromGameState(game: Game): String {
        var fenCastle = ""
        if (game.castling.isWhiteKingCastlePossible) {
            fenCastle += "K"
        }
        if (game.castling.isWhiteQueenCastlePossible) {
            fenCastle += "Q"
        }
        if (game.castling.isBlackKingCastlePossible) {
            fenCastle += "k"
        }
        if (game.castling.isBlackQueenCastlePossible) {
            fenCastle += "q"
        }
        return fenCastle.ifEmpty {
            "-"
        }
    }

    private fun getFenPiecePlacementFromGameState(game: Game): String {
        var chessboardFinished = false
        var currentSquare = Square.A8
        var fen = ""
        while (!chessboardFinished) {
            var lineFinished = false
            var currentGap = 0
            while (!lineFinished) {
                val position = game.chessboard.getPositionAt(currentSquare)
                if (position != null) {
                    if (currentGap != 0) {
                        fen += currentGap
                    }
                    fen += getLetter(position.piece)
                    currentGap = 0
                } else {
                    currentGap++
                }
                // next square
                if (currentSquare.right() != null) {
                    currentSquare = currentSquare.right()!!
                } else {
                    lineFinished = true
                    if (currentGap != 0) {
                        fen += currentGap
                    }
                    if (currentSquare.down() != null) {
                        fen += "/"
                        currentSquare = currentSquare.down()!!.toTheVeryLeft()
                    } else {
                        chessboardFinished = true
                    }
                }
            }
        }
        return fen
    }

    private fun enPassantTargetSquare(enPassantFen: String): Square? {
        return if (enPassantFen.length == 2) {
            Square.at(enPassantFen)
        } else {
            null
        }
    }

    private fun sideToMove(sideToMove: String): Color {
        return if (sideToMove.lowercase(Locale.getDefault()) == "w") {
            return WHITE
        } else {
            BLACK
        }
    }

    fun getChessboardFromFenPiecePlacement(fenPiecePlacement: String): Chessboard {
        val board: MutableList<Position> = mutableListOf()
        val lines = fenPiecePlacement.split("/")
        var currentSquare = Square.A8
        for (lineIndex in 8 downTo 1) {
            val currentLine = lines[8 - lineIndex]
            currentLine.forEach { currentChar ->
                var currentIndex = currentChar.toString().toIntOrNull()
                if (currentIndex == null) {
                    board.add(Position(currentSquare, getPiece(currentChar.toString())))
                    if (currentSquare.file != File.FILE_H) {
                        currentSquare = currentSquare.right()!!
                    }
                } else
                    while (currentIndex > 0) {
                        if (currentSquare.right() != null) {
                            currentSquare = currentSquare.right()!!
                        }
                        currentIndex--
                    }

            }
            currentSquare = currentSquare.toTheVeryLeft()
            if (currentSquare.rank != Rank.RANK_1) {
                currentSquare = currentSquare.down()!!
            }
        }
        return Chessboard(piecesOnBoard = board)
    }

    private fun getPiece(str: String): Piece {
        return when (str) {
            "r" -> BLACK_ROOK
            "R" -> WHITE_ROOK
            "n" -> BLACK_KNIGHT
            "N" -> WHITE_KNIGHT
            "b" -> BLACK_BISHOP
            "B" -> WHITE_BISHOP
            "q" -> BLACK_QUEEN
            "Q" -> WHITE_QUEEN
            "k" -> BLACK_KING
            "K" -> WHITE_KING
            "p" -> BLACK_PAWN
            "P" -> WHITE_PAWN
            else -> throw IllegalArgumentException("Unknown fen piece format : $str")
        }
    }

    private fun getLetter(piece: Piece): String {
        return when (piece) {
            BLACK_ROOK -> "r"
            WHITE_ROOK -> "R"
            BLACK_KNIGHT -> "n"
            WHITE_KNIGHT -> "N"
            BLACK_BISHOP -> "b"
            WHITE_BISHOP -> "B"
            BLACK_QUEEN -> "q"
            WHITE_QUEEN -> "Q"
            BLACK_KING -> "k"
            WHITE_KING -> "K"
            BLACK_PAWN -> "p"
            WHITE_PAWN -> "P"
        }
    }
}
