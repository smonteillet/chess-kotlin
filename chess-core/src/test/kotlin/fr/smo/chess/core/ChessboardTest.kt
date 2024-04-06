package fr.smo.chess.core

import fr.smo.chess.core.Piece.WHITE_ROOK
import fr.smo.chess.core.Square.A1
import fr.smo.chess.core.Square.E5
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class ChessboardTest {

    private val board = Chessboard(
        piecesOnBoard = mapOf(
                A1 to  WHITE_ROOK
        )
    )


    @Test
    fun `get existing Position at corresponding square returns piece`() {
        val foundPosition = board.getPieceAt(A1)
        assertEquals(foundPosition, WHITE_ROOK)
    }

    @Test
    fun `get non existing Position at an empty square returns null`() {
        val foundPosition = board.getPieceAt(E5)
        assertNull(foundPosition)
    }
}