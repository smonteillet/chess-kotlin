package fr.smo.chess.service

import fr.smo.chess.model.Color.BLACK
import fr.smo.chess.model.Color.WHITE
import fr.smo.chess.model.GameState
import fr.smo.chess.model.Piece.Companion.blackRook
import fr.smo.chess.model.Piece.Companion.whiteKing
import fr.smo.chess.model.Piece.Companion.whitePawn
import fr.smo.chess.model.Piece.Companion.whiteQueen
import fr.smo.chess.model.Position
import fr.smo.chess.model.Square.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo


class FENTest {

    private val fen = FEN()

    @Nested
    inner class Export {

        @Test
        fun `should export to starting position`() {
            // Given
            val gameState = GameState.NEW_STANDARD_CHESS_GAME
            // When
            val fen = fen.export(gameState)
            // Then
            expectThat(fen) isEqualTo STARTING_POSITION_FEN
        }

    }

    @Nested
    inner class Import {
        @Nested
        inner class PiecePlacement {

            @Test
            fun `should import initial position for chessboard`() {
                // Given
                val fen = STARTING_POSITION_FEN
                // When
                val gameState = this@FENTest.fen.import(fen)
                // Then
                expectThat(gameState.chessboard.piecesOnBoard).containsExactlyInAnyOrder(GameState.NEW_STANDARD_CHESS_GAME.chessboard.piecesOnBoard)
            }

            @Test
            fun `should import empty chessboard`() {
                // Given
                val fen = "8/8/8/8/8/8/8/8 w KQkq - 0 1"
                // When
                val gameState = this@FENTest.fen.import(fen)
                // Then
                expectThat(gameState.chessboard.piecesOnBoard).isEmpty()
            }

            @Test
            fun `should import a complex line starting with a piece`() {
                // Given
                val fen = "r3r2Q/8/8/8/8/8/8/8 w KQkq - 0 1"
                // When
                val gameState = this@FENTest.fen.import(fen)
                // Then
                expectThat(gameState.chessboard.piecesOnBoard).containsExactlyInAnyOrder(
                    Position(A8, blackRook()),
                    Position(E8, blackRook()),
                    Position(H8, whiteQueen())
                )
            }

            @Test
            fun `should import simple fen`() {
                // Given
                val fen = "8/8/8/8/8/8/P7/K7 w KQkq - 0 1"
                // When
                val gameState = this@FENTest.fen.import(fen)
                // Then
                expectThat(gameState.chessboard.piecesOnBoard).containsExactlyInAnyOrder(
                    Position(A1, whiteKing()),
                    Position(A2, whitePawn()),
                )
            }

            @Test
            fun `should import a complex line starting with an empty square`() {
                // Given
                val fen = "1Q6/8/8/8/8/8/8/8 w KQkq - 0 1"
                // When
                val gameState = this@FENTest.fen.import(fen)
                // Then
                expectThat(gameState.chessboard.piecesOnBoard).containsExactlyInAnyOrder(
                    Position(B8, whiteQueen())
                )
            }
        }

        @Nested
        inner class SideToMove {
            @Test
            fun `should return white to move`() {
                // Given
                val fen = STARTING_POSITION_FEN
                // When
                val gameState = this@FENTest.fen.import(fen)
                // Then
                expectThat(gameState.sideToMove) isEqualTo WHITE
            }

            @Test
            fun `should return black to move`() {
                // Given
                val fen = "r3r2Q/8/8/8/8/8/8/8 b KQkq - 0 1"
                // When
                val gameState = this@FENTest.fen.import(fen)
                // Then
                expectThat(gameState.sideToMove) isEqualTo BLACK
            }
        }

        @Nested
        inner class CastlingAbility {
            @Test
            fun `should mark all castling available at game start`() {
                // Given
                val fen = STARTING_POSITION_FEN
                // When
                val gameState = this@FENTest.fen.import(fen)
                // Then
                expectThat(gameState.isBlackQueenCastlePossible) isEqualTo true
                expectThat(gameState.isBlackKingCastlePossible) isEqualTo true
                expectThat(gameState.isWhiteQueenCastlePossible) isEqualTo true
                expectThat(gameState.isWhiteKingCastlePossible) isEqualTo true
            }

            @Test
            fun `should mark white king castle when possible`() {
                // Given
                val fen = "k7/8/8/8/8/8/8/4K2R b K - 0 1"
                // When
                val gameState = this@FENTest.fen.import(fen)
                // Then
                expectThat(gameState.isBlackQueenCastlePossible) isEqualTo false
                expectThat(gameState.isBlackKingCastlePossible) isEqualTo false
                expectThat(gameState.isWhiteQueenCastlePossible) isEqualTo false
                expectThat(gameState.isWhiteKingCastlePossible) isEqualTo true
            }

            @Test
            fun `should mark white queen castle when possible`() {
                // Given
                val fen = "1k6/8/8/8/8/8/8/R3K3 b Q - 0 1"
                // When
                val gameState = this@FENTest.fen.import(fen)
                // Then
                expectThat(gameState.isBlackQueenCastlePossible) isEqualTo false
                expectThat(gameState.isBlackKingCastlePossible) isEqualTo false
                expectThat(gameState.isWhiteQueenCastlePossible) isEqualTo true
                expectThat(gameState.isWhiteKingCastlePossible) isEqualTo false
            }

            @Test
            fun `should mark all white castle when possible`() {
                // Given
                val fen = "1k6/8/8/8/8/8/8/R3K2R b KQ - 0 1"
                // When
                val gameState = this@FENTest.fen.import(fen)
                // Then
                expectThat(gameState.isBlackQueenCastlePossible) isEqualTo false
                expectThat(gameState.isBlackKingCastlePossible) isEqualTo false
                expectThat(gameState.isWhiteQueenCastlePossible) isEqualTo true
                expectThat(gameState.isWhiteKingCastlePossible) isEqualTo true
            }

            @Test
            fun `should mark black king castle when possible`() {
                // Given
                val fen = "k7/8/8/8/8/8/8/4K2R b k - 0 1"
                // When
                val gameState = this@FENTest.fen.import(fen)
                // Then
                expectThat(gameState.isBlackQueenCastlePossible) isEqualTo false
                expectThat(gameState.isBlackKingCastlePossible) isEqualTo true
                expectThat(gameState.isWhiteQueenCastlePossible) isEqualTo false
                expectThat(gameState.isWhiteKingCastlePossible) isEqualTo false
            }

            @Test
            fun `should mark black queen castle when possible`() {
                // Given
                val fen = "1k6/8/8/8/8/8/8/R3K3 b q - 0 1"
                // When
                val gameState = this@FENTest.fen.import(fen)
                // Then
                expectThat(gameState.isBlackQueenCastlePossible) isEqualTo true
                expectThat(gameState.isBlackKingCastlePossible) isEqualTo false
                expectThat(gameState.isWhiteQueenCastlePossible) isEqualTo false
                expectThat(gameState.isWhiteKingCastlePossible) isEqualTo false
            }

            @Test
            fun `should mark no castle when possible`() {
                // Given
                val fen = "1k6/8/8/8/8/8/8/R3K3 b - - 0 1"
                // When
                val gameState = this@FENTest.fen.import(fen)
                // Then
                expectThat(gameState.isBlackQueenCastlePossible) isEqualTo false
                expectThat(gameState.isBlackKingCastlePossible) isEqualTo false
                expectThat(gameState.isWhiteQueenCastlePossible) isEqualTo false
                expectThat(gameState.isWhiteKingCastlePossible) isEqualTo false
            }
        }

        @Nested
        inner class EnPassantTargetSquare {

            @Test
            fun `should not mark en passant for when not mentionned`() {
                // Given
                val fen = STARTING_POSITION_FEN
                // When
                val gameState = this@FENTest.fen.import(fen)
                // Then
                expectThat(gameState.enPassantTargetSquare) isEqualTo null
            }

            @Test
            fun `should mark en passant for black when available`() {
                // Given
                val fen = "rnbqkbnr/p1pppppp/8/1p6/8/8/PPPPPPPP/RNBQKBNR w KQkq b6 0 1"
                // When
                val gameState = this@FENTest.fen.import(fen)
                // Then
                expectThat(gameState.enPassantTargetSquare) isEqualTo B6
            }

            @Test
            fun `should mark en passant for white when available`() {
                // Given
                val fen = "rnbqkbnr/pppppppp/8/8/1P6/8/P1PPPPPP/RNBQKBNR w KQkq b3 0 1"
                // When
                val gameState = this@FENTest.fen.import(fen)
                // Then
                expectThat(gameState.enPassantTargetSquare) isEqualTo B3
            }
        }

        /**
        The halfmove clock specifies a decimal number of half moves with respect to the 50 move draw rule.
        It is reset to zero after a capture or a pawn move and incremented otherwise.
        <Halfmove Clock> ::= <digit> {<digit>}
        <digit> ::= '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9'
         */
        @Nested
        inner class HalfmoveClock {

            @Test
            fun `should mark to 0 half move clock count at game start`() {
                // Given
                val fen = STARTING_POSITION_FEN
                // When
                val gameState = this@FENTest.fen.import(fen)
                // Then
                expectThat(gameState.halfMoveClock) isEqualTo 0
            }

            @Test
            fun `should mark half move clock count to value indicated in FEN`() {
                // Given
                val fen = "k7/8/8/8/8/8/8/4K2R b k - 21 1"
                // When
                val gameState = this@FENTest.fen.import(fen)
                // Then
                expectThat(gameState.halfMoveClock) isEqualTo 21
            }
        }

        @Nested
        inner class FullmoveClock {
            /**
             * The number of the full moves in a game. It starts at 1, and is incremented after each Black's move.
            <Fullmove counter> ::= <digit19> {<digit>}
            <digit19> ::= '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9'
            <digit>   ::= '0' | <digit19>
             */
            @Test
            fun `should mark to 1 full clock count at game start`() {
                // Given
                val fen = STARTING_POSITION_FEN
                // When
                val gameState = this@FENTest.fen.import(fen)
                // Then
                expectThat(gameState.fullMoveCounter) isEqualTo 1
            }

            @Test
            fun `should mark full clock count to value indicated in FEN`() {
                // Given
                val fen = "k7/8/8/8/8/8/8/4K2R b k - 21 39"
                // When
                val gameState = this@FENTest.fen.import(fen)
                // Then
                expectThat(gameState.fullMoveCounter) isEqualTo 39
            }
        }
    }
}