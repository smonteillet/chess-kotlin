package fr.smo.chess.core.notation

import fr.smo.chess.core.Color.BLACK
import fr.smo.chess.core.Color.WHITE
import fr.smo.chess.core.GameFactory
import fr.smo.chess.core.Piece.*
import fr.smo.chess.core.Position
import fr.smo.chess.core.Square.*
import fr.smo.chess.core.notation.FEN.exportFEN
import fr.smo.chess.core.notation.FEN.importFEN
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo


class FENTest {

    @Nested
    inner class Export {

        @Test
        fun `should export to starting position`() {
            // Given
            val game = GameFactory.createStandardGame()
            // When
            val fen = exportFEN(game)
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
                val game = importFEN(fen)
                // Then
                expectThat(game.chessboard.piecesOnBoard).containsExactlyInAnyOrder(GameFactory.createStandardGame().chessboard.piecesOnBoard)
            }

            @Test
            fun `should import empty chessboard`() {
                // Given
                val fen = "8/8/8/8/8/8/8/8 w KQkq - 0 1"
                // When
                val game = importFEN(fen)
                // Then
                expectThat(game.chessboard.piecesOnBoard).isEmpty()
            }

            @Test
            fun `should import a complex line starting with a piece`() {
                // Given
                val fen = "r3r2Q/8/8/8/8/8/8/8 w KQkq - 0 1"
                // When
                val game = importFEN(fen)
                // Then
                expectThat(game.chessboard.piecesOnBoard).containsExactlyInAnyOrder(
                    Position(A8, BLACK_ROOK),
                    Position(E8, BLACK_ROOK),
                    Position(H8, WHITE_QUEEN)
                )
            }

            @Test
            fun `should import simple fen`() {
                // Given
                val fen = "8/8/8/8/8/8/P7/K7 w KQkq - 0 1"
                // When
                val game = importFEN(fen)
                // Then
                expectThat(game.chessboard.piecesOnBoard).containsExactlyInAnyOrder(
                    Position(A1, WHITE_KING),
                    Position(A2, WHITE_PAWN),
                )
            }

            @Test
            fun `should import a complex line starting with an empty square`() {
                // Given
                val fen = "1Q6/8/8/8/8/8/8/8 w KQkq - 0 1"
                // When
                val game = importFEN(fen)
                // Then
                expectThat(game.chessboard.piecesOnBoard).containsExactlyInAnyOrder(
                    Position(B8, WHITE_QUEEN)
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
                val game = importFEN(fen)
                // Then
                expectThat(game.sideToMove) isEqualTo WHITE
            }

            @Test
            fun `should return black to move`() {
                // Given
                val fen = "r3r2Q/8/8/8/8/8/8/8 b KQkq - 0 1"
                // When
                val game = importFEN(fen)
                // Then
                expectThat(game.sideToMove) isEqualTo BLACK
            }
        }

        @Nested
        inner class CastlingAbility {
            @Test
            fun `should mark all castling available at game start`() {
                // Given
                val fen = STARTING_POSITION_FEN
                // When
                val game = importFEN(fen)
                // Then
                expectThat(game.castling.isBlackQueenCastlePossible) isEqualTo true
                expectThat(game.castling.isBlackKingCastlePossible) isEqualTo true
                expectThat(game.castling.isWhiteQueenCastlePossible) isEqualTo true
                expectThat(game.castling.isWhiteKingCastlePossible) isEqualTo true
            }

            @Test
            fun `should mark white king castle when possible`() {
                // Given
                val fen = "k7/8/8/8/8/8/8/4K2R b K - 0 1"
                // When
                val game = importFEN(fen)
                // Then
                expectThat(game.castling.isBlackQueenCastlePossible) isEqualTo false
                expectThat(game.castling.isBlackKingCastlePossible) isEqualTo false
                expectThat(game.castling.isWhiteQueenCastlePossible) isEqualTo false
                expectThat(game.castling.isWhiteKingCastlePossible) isEqualTo true
            }

            @Test
            fun `should mark white queen castle when possible`() {
                // Given
                val fen = "1k6/8/8/8/8/8/8/R3K3 b Q - 0 1"
                // When
                val game = importFEN(fen)
                // Then
                expectThat(game.castling.isBlackQueenCastlePossible) isEqualTo false
                expectThat(game.castling.isBlackKingCastlePossible) isEqualTo false
                expectThat(game.castling.isWhiteQueenCastlePossible) isEqualTo true
                expectThat(game.castling.isWhiteKingCastlePossible) isEqualTo false
            }

            @Test
            fun `should mark all white castle when possible`() {
                // Given
                val fen = "1k6/8/8/8/8/8/8/R3K2R b KQ - 0 1"
                // When
                val game = importFEN(fen)
                // Then
                expectThat(game.castling.isBlackQueenCastlePossible) isEqualTo false
                expectThat(game.castling.isBlackKingCastlePossible) isEqualTo false
                expectThat(game.castling.isWhiteQueenCastlePossible) isEqualTo true
                expectThat(game.castling.isWhiteKingCastlePossible) isEqualTo true
            }

            @Test
            fun `should mark black king castle when possible`() {
                // Given
                val fen = "k7/8/8/8/8/8/8/4K2R b k - 0 1"
                // When
                val game = importFEN(fen)
                // Then
                expectThat(game.castling.isBlackQueenCastlePossible) isEqualTo false
                expectThat(game.castling.isBlackKingCastlePossible) isEqualTo true
                expectThat(game.castling.isWhiteQueenCastlePossible) isEqualTo false
                expectThat(game.castling.isWhiteKingCastlePossible) isEqualTo false
            }

            @Test
            fun `should mark black queen castle when possible`() {
                // Given
                val fen = "1k6/8/8/8/8/8/8/R3K3 b q - 0 1"
                // When
                val game = importFEN(fen)
                // Then
                expectThat(game.castling.isBlackQueenCastlePossible) isEqualTo true
                expectThat(game.castling.isBlackKingCastlePossible) isEqualTo false
                expectThat(game.castling.isWhiteQueenCastlePossible) isEqualTo false
                expectThat(game.castling.isWhiteKingCastlePossible) isEqualTo false
            }

            @Test
            fun `should mark no castle when possible`() {
                // Given
                val fen = "1k6/8/8/8/8/8/8/R3K3 b - - 0 1"
                // When
                val game = importFEN(fen)
                // Then
                expectThat(game.castling.isBlackQueenCastlePossible) isEqualTo false
                expectThat(game.castling.isBlackKingCastlePossible) isEqualTo false
                expectThat(game.castling.isWhiteQueenCastlePossible) isEqualTo false
                expectThat(game.castling.isWhiteKingCastlePossible) isEqualTo false
            }
        }

        @Nested
        inner class EnPassantTargetSquare {

            @Test
            fun `should not mark en passant for when not mentionned`() {
                // Given
                val fen = STARTING_POSITION_FEN
                // When
                val game = importFEN(fen)
                // Then
                expectThat(game.enPassantTargetSquare) isEqualTo null
            }

            @Test
            fun `should mark en passant for black when available`() {
                // Given
                val fen = "rnbqkbnr/p1pppppp/8/1p6/8/8/PPPPPPPP/RNBQKBNR w KQkq b6 0 1"
                // When
                val game = importFEN(fen)
                // Then
                expectThat(game.enPassantTargetSquare) isEqualTo B6
            }

            @Test
            fun `should mark en passant for white when available`() {
                // Given
                val fen = "rnbqkbnr/pppppppp/8/8/1P6/8/P1PPPPPP/RNBQKBNR w KQkq b3 0 1"
                // When
                val game = importFEN(fen)
                // Then
                expectThat(game.enPassantTargetSquare) isEqualTo B3
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
                val game = importFEN(fen)
                // Then
                expectThat(game.history.halfMoveClock) isEqualTo 0
            }

            @Test
            fun `should mark half move clock count to value indicated in FEN`() {
                // Given
                val fen = "k7/8/8/8/8/8/8/4K2R b k - 21 1"
                // When
                val game = importFEN(fen)
                // Then
                expectThat(game.history.halfMoveClock) isEqualTo 21
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
                val game = importFEN(fen)
                // Then
                expectThat(game.history.fullMoveCounter) isEqualTo 1
            }

            @Test
            fun `should mark full clock count to value indicated in FEN`() {
                // Given
                val fen = "k7/8/8/8/8/8/8/4K2R b k - 21 39"
                // When
                val game = importFEN(fen)
                // Then
                expectThat(game.history.fullMoveCounter) isEqualTo 39
            }
        }
    }
}