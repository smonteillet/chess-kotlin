package fr.smo.chess.model

import fr.smo.chess.model.Color.WHITE
import fr.smo.chess.model.Piece.Companion.piece
import fr.smo.chess.model.Piece.Type.ROOK
import fr.smo.chess.model.Square.Companion.square
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class ChessboardTest {

    private val board = Chessboard.initNewBoard()

    @Test
    fun `get existing Position at corresponding square returns piece`() {
        val foundPosition = board.getPositionAt(square("a1"))
        assertEquals(foundPosition, Chessboard.Position(square("a1"), piece(ROOK, WHITE)))
    }

    @Test
    fun `get non existing Position at an empty square returns null`() {
        val foundPosition = board.getPositionAt(square("e5"))
        assertNull(foundPosition)
    }
}