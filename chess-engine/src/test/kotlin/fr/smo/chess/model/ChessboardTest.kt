package fr.smo.chess.model

import fr.smo.chess.model.Color.WHITE
import fr.smo.chess.model.Piece.Companion.piece
import fr.smo.chess.model.Piece.Type.ROOK
import fr.smo.chess.model.Square.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class ChessboardTest {

    private val board = GameState.NEW_STANDARD_CHESS_GAME.chessboard

    @Test
    fun `get existing Position at corresponding square returns piece`() {
        val foundPosition = board.getPositionAt(A1)
        assertEquals(foundPosition, Position(A1, piece(ROOK, WHITE)))
    }

    @Test
    fun `get non existing Position at an empty square returns null`() {
        val foundPosition = board.getPositionAt(E5)
        assertNull(foundPosition)
    }
}