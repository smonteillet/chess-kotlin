package fr.smo.chess.model

import fr.smo.chess.model.Piece.Companion.blackBishop
import fr.smo.chess.model.Piece.Companion.blackKing
import fr.smo.chess.model.Piece.Companion.blackKnight
import fr.smo.chess.model.Piece.Companion.blackPawn
import fr.smo.chess.model.Piece.Companion.blackQueen
import fr.smo.chess.model.Piece.Companion.blackRook
import fr.smo.chess.model.Piece.Companion.whiteBishop
import fr.smo.chess.model.Piece.Companion.whiteKing
import fr.smo.chess.model.Piece.Companion.whiteKnight
import fr.smo.chess.model.Piece.Companion.whitePawn
import fr.smo.chess.model.Piece.Companion.whiteQueen
import fr.smo.chess.model.Piece.Companion.whiteRook
import fr.smo.chess.model.Square.Companion.square

data class Chessboard(
    val piecesOnBoard: List<Position> = listOf(),
) {

    data class Position(val square: Square, val piece: Piece)


    companion object {
        fun initNewBoard(): Chessboard {
            val board: MutableList<Position> = mutableListOf()
            board.add(Position(square("a1"), whiteRook()))
            board.add(Position(square("b1"), whiteKnight()))
            board.add(Position(square("c1"), whiteBishop()))
            board.add(Position(square("d1"), whiteQueen()))
            board.add(Position(square("e1"), whiteKing()))
            board.add(Position(square("f1"), whiteBishop()))
            board.add(Position(square("g1"), whiteKnight()))
            board.add(Position(square("h1"), whiteRook()))
            board.add(Position(square("a2"), whitePawn()))
            board.add(Position(square("b2"), whitePawn()))
            board.add(Position(square("c2"), whitePawn()))
            board.add(Position(square("d2"), whitePawn()))
            board.add(Position(square("e2"), whitePawn()))
            board.add(Position(square("f2"), whitePawn()))
            board.add(Position(square("g2"), whitePawn()))
            board.add(Position(square("h2"), whitePawn()))
            board.add(Position(square("a8"), blackRook()))
            board.add(Position(square("b8"), blackKnight()))
            board.add(Position(square("c8"), blackBishop()))
            board.add(Position(square("d8"), blackQueen()))
            board.add(Position(square("e8"), blackKing()))
            board.add(Position(square("f8"), blackBishop()))
            board.add(Position(square("g8"), blackKnight()))
            board.add(Position(square("h8"), blackRook()))
            board.add(Position(square("a7"), blackPawn()))
            board.add(Position(square("b7"), blackPawn()))
            board.add(Position(square("c7"), blackPawn()))
            board.add(Position(square("d7"), blackPawn()))
            board.add(Position(square("e7"), blackPawn()))
            board.add(Position(square("f7"), blackPawn()))
            board.add(Position(square("g7"), blackPawn()))
            board.add(Position(square("h7"), blackPawn()))
            return Chessboard(piecesOnBoard = board)
        }
    }

    fun isPiecePresentAtAndHasColor(square: Square, color: Color) : Boolean = getPositionAt(square)?.piece?.color == color

    fun getPositionAt(square: Square): Position? {
        return this.piecesOnBoard.firstOrNull { it.square == square }
    }

}
