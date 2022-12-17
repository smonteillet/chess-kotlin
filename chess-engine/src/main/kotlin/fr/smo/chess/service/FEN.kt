package fr.smo.chess.service

import fr.smo.chess.model.*
import fr.smo.chess.model.Color.BLACK
import fr.smo.chess.model.Color.WHITE
import fr.smo.chess.model.Piece.Companion.piece
import fr.smo.chess.model.Piece.Type.*
import fr.smo.chess.model.Square.Companion.square
import java.util.*

const val STARTING_POSITION_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

class FEN {

    fun import(fen: String) : GameState {
        val fenSplit = fen.split(" ")
        require(fenSplit.size == 6)
        return GameState(
            chessboard = getChessboardFromFenPiecePlacement(fenSplit[0]),
            sideToMove = sideToMove(fenSplit[1]),
            isBlackKingCastlePossible = fenSplit[2].contains("k"),
            isBlackQueenCastlePossible = fenSplit[2].contains("q"),
            isWhiteQueenCastlePossible = fenSplit[2].contains("Q"),
            isWhiteKingCastlePossible = fenSplit[2].contains("K"),
            enPassantTargetSquare = enPassantTargetSquare(fenSplit[3]),
            halfMoveClock = fenSplit[4].toInt(),
            fullMoveCounter = fenSplit[5].toInt(),
        )
    }

    fun export(gameState: GameState) : String {
       return getFenPiecePlacementFromGameState(gameState) + " " +
               getFenSideToMoveFromGameState(gameState)  + " " +
               getFenCastlingFromGameState(gameState)  + " " +
               (gameState.enPassantTargetSquare?.toString() ?: "-") + " " +
               gameState.halfMoveClock + " " +
               gameState.fullMoveCounter
    }

    private fun getFenSideToMoveFromGameState(gameState: GameState) : String {
        return if(gameState.sideToMove == WHITE) {
            "w"
        }
        else {
            "b"
        }
    }

    private fun getFenCastlingFromGameState(gameState: GameState) : String {
        var fenCastle = ""
        if (gameState.isWhiteKingCastlePossible) {
            fenCastle += "K"
        }
        if (gameState.isWhiteQueenCastlePossible) {
            fenCastle += "Q"
        }
        if (gameState.isBlackKingCastlePossible) {
            fenCastle += "k"
        }
        if (gameState.isBlackQueenCastlePossible) {
            fenCastle += "q"
        }
        return fenCastle.ifEmpty {
            "-"
        }
    }

    private fun getFenPiecePlacementFromGameState(gameState: GameState) : String {
        var chessboardFinished = false
        var currentSquare = square("a8")
        var fen = ""
        while (!chessboardFinished) {
            var lineFinished = false
            var currentGap = 0
            while (!lineFinished) {
                val position = gameState.chessboard.getPositionAt(currentSquare)
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
                    }
                    else {
                        chessboardFinished = true
                    }
                }
            }
        }
        return fen
    }

    private fun enPassantTargetSquare(enPassantFen: String): Square? {
        return if (enPassantFen.length == 2) {
            square(enPassantFen)
        }
        else {
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

    fun getChessboardFromFenPiecePlacement(fenPiecePlacement : String): Chessboard {
        val board: MutableList<Position> = mutableListOf()
        val lines = fenPiecePlacement.split("/")
        var currentSquare = square("a8")
        for (lineIndex in 8 downTo 1) {
            val currentLine = lines[8 - lineIndex]
            currentLine.forEach { currentChar ->
                var currentIndex = currentChar.toString().toIntOrNull()
                if (currentIndex == null) {
                    board.add(Position(currentSquare,getPiece(currentChar.toString())))
                    if (currentSquare.getColumn() != "H") {
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
            if (currentSquare.row != 1) {
                currentSquare = currentSquare.down()!!
            }
        }
        return Chessboard(piecesOnBoard = board,)
    }

    private fun getPiece(str : String) : Piece {
        return when (str) {
            "r" -> piece(ROOK,   BLACK)
            "R" -> piece(ROOK,   WHITE)
            "n" -> piece(KNIGHT, BLACK)
            "N" -> piece(KNIGHT, WHITE)
            "b" -> piece(BISHOP, BLACK)
            "B" -> piece(BISHOP, WHITE)
            "q" -> piece(QUEEN,  BLACK)
            "Q" -> piece(QUEEN,  WHITE)
            "k" -> piece(KING,   BLACK)
            "K" -> piece(KING,   WHITE)
            "p" -> piece(PAWN,   BLACK)
            "P" -> piece(PAWN,   WHITE)
            else -> throw IllegalArgumentException("Unknown fen piece format : $str")
        }
    }

    private fun getLetter(piece : Piece) : String {
        return when (piece) {
            piece(ROOK,   BLACK) -> "r"
            piece(ROOK,   WHITE) -> "R"
            piece(KNIGHT, BLACK) -> "n"
            piece(KNIGHT, WHITE) -> "N"
            piece(BISHOP, BLACK) -> "b"
            piece(BISHOP, WHITE) -> "B"
            piece(QUEEN,  BLACK) -> "q"
            piece(QUEEN,  WHITE) -> "Q"
            piece(KING,   BLACK) -> "k"
            piece(KING,   WHITE) -> "K"
            piece(PAWN,   BLACK) -> "p"
            piece(PAWN,   WHITE) -> "P"
            else -> throw IllegalArgumentException("Unknown fen piece format : $piece")
        }
    }
}