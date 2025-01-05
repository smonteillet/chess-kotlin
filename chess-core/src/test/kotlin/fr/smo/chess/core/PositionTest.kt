package fr.smo.chess.core

import fr.smo.chess.core.Color.BLACK
import fr.smo.chess.core.Color.WHITE
import fr.smo.chess.core.Piece.*
import fr.smo.chess.core.PieceType.*
import fr.smo.chess.core.Square.*
import fr.smo.chess.core.Status.*
import fr.smo.chess.core.fixtures.GameStateFixtures.givenAChessGame
import fr.smo.chess.core.utils.Failure
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.*

class PositionTest {

    @Nested
    inner class CheckMate {


        @Test
        fun `should not mark game as over after check but not checkmate`() {
            // Given When
            val game = givenAChessGame().applyMoves(
                MoveCommand(E2, E3),
                MoveCommand(F7, F6),
                MoveCommand(D1, H5),
            ).orThrow()
            expectThat(game.status.gameIsOver).isFalse()
        }

        @Test
        fun `should not mark game as over when a check can be escaped`() {
            // Given When
            val game = givenAChessGame(fenPiecePlacementOnly = "8/8/8/8/3n4/8/8/K7", sideToMove = BLACK).applyMove(
                MoveCommand(
                    D4,
                    C2
                )
            ).orThrow()
            // Then
            expectThat(game.status.gameIsOver).isFalse()
        }

        @Test
        fun `should mark game as won when performing Scholar's mate`() {
            // Given When
            val game = givenAChessGame().applyMoves(
                MoveCommand(E2, E4),
                MoveCommand(E7, E5),
                MoveCommand(F1, C4),
                MoveCommand(B8, C6),
                MoveCommand(D1, H5),
                MoveCommand(G8, F6),
                MoveCommand(H5, F7),
            ).orThrow()
            // Then
            expectThat(game.status) isEqualTo WHITE_WIN
        }

        @Test
        fun `should mark game as won when performing back rank mate`() {
            // Given When
            val game =
                givenAChessGame(fenPiecePlacementOnly = "4k2r/p7/8/8/8/8/PPP5/1K6", sideToMove = BLACK).applyMove(
                    MoveCommand(H8, H1)
                ).orThrow()
            // Then
            expectThat(game.status) isEqualTo BLACK_WIN
        }

        @Test
        fun `should mark game as won when performing kiss of death checkmate`() {
            // Given When
            val game = givenAChessGame(fenPiecePlacementOnly = "8/8/8/7P/8/1k6/4q3/1K6", sideToMove = BLACK).applyMove(
                MoveCommand(E2, B2)
            ).orThrow()
            // Then
            expectThat(game.status) isEqualTo BLACK_WIN
        }

        @Test
        fun `should mark game as won when performing smothered mate checkmate`() {
            /// Given When
            val game =
                givenAChessGame(fenPiecePlacementOnly = "6rk/6pp/2PN4/8/8/8/8/1K6", sideToMove = WHITE).applyMove(
                    MoveCommand(D6, F7)
                ).orThrow()
            // Then
            expectThat(game.status) isEqualTo WHITE_WIN
        }

        @Test
        fun `should mark game as won when performing two rook checkmate`() {
            // Given When
            val game =
                givenAChessGame(fenPiecePlacementOnly = "8/3R4/8/7k/8/1p4R1/8/3K4", sideToMove = WHITE).applyMove(
                    MoveCommand(D7, H7)
                ).orThrow()
            // Then
            expectThat(game.status) isEqualTo WHITE_WIN
        }

        @Test
        fun `should mark game as won when performing rook + king checkmate`() {
            /// Given When
            val game = givenAChessGame(fenPiecePlacementOnly = "8/8/8/8/8/2k5/7r/2K5", sideToMove = BLACK).applyMove(
                MoveCommand(H2, H1)
            ).orThrow()
            // Then
            expectThat(game.status) isEqualTo BLACK_WIN
        }

        @Test
        fun `should mark game as won after a promotion checkmate`() {
            // Given When
            val game = givenAChessGame(fenPiecePlacementOnly = "8/8/8/8/8/2k5/7p/2K5", sideToMove = BLACK).applyMove(
                MoveCommand(H2, H1, QUEEN)
            ).orThrow()
            // Then
            expectThat(game.status) isEqualTo BLACK_WIN
        }

        @Test
        fun `should mark as won after a pinned pawn checkmate`() {
            // Given When
            val game = givenAChessGame(
                fenPiecePlacementOnly = "7k/6p1/7r/8/2Q4R/2B5/8/8", sideToMove = WHITE
            ).applyMove(MoveCommand(H4, H6)).orThrow()
            // Then
            expectThat(game.status) isEqualTo WHITE_WIN

        }
    }

    @Nested
    inner class CasualMove {

        @Test
        fun `should pawn moved without capture lead to no pieces amount changed on chessboard`() {
            // Given When
            val game = givenAChessGame().applyMoves(
                MoveCommand(H2, H4), MoveCommand(H7, H5)
            ).orThrow()
            // Then
            expectThat(game.history.moves.none { it.capturedPiece != null })
            expectThat(game.chessboard.numberOfRemainingPieces()) isEqualTo 32
        }
    }

    @Nested
    inner class CheckSituation {

        @Test
        fun `should throw an error when performing a move that doesn't revert check when king is checked`() {
            // Given When
            val game =
                givenAChessGame(fenPiecePlacementOnly = "3k4/1P6/8/8/8/8/7r/2K5", sideToMove = BLACK).applyMove(
                    MoveCommand(H2, H1)
                ).orThrow() // White King check
            // Then
            expectThat(game.applyMove(MoveCommand(B7, B8))).isA<Failure<MoveCommandError>>()
        }

        @Test
        fun `should throw an error when performing a move that doesn't revert check when king is checked after promotion`() {
            // Given When
            val game =
                givenAChessGame(fenPiecePlacementOnly = "3k4/1P6/8/8/8/8/7p/2K5", sideToMove = BLACK).applyMove(
                    MoveCommand(H2, H1, QUEEN)
                ).orThrow() // White King check

            // Then
            expectThat(game.applyMove(MoveCommand(B7, B8))).isA<Failure<MoveCommandError>>()

        }

    }

    @Nested
    inner class Promotion {
        @Test
        fun `should black pawn promote to a Queen when possible`() {
            // Given When
            val game = givenAChessGame(fenPiecePlacementOnly = "4k3/P7/8/8/8/8/1p6/4K3", sideToMove = BLACK).applyMove(
                MoveCommand(B2, B1, QUEEN)
            ).orThrow()
            // Then
            expectThat(game.chessboard.getPieceAt(B1)) isEqualTo BLACK_QUEEN
            expectThat(game.chessboard.count { it.value == BLACK_PAWN }) isEqualTo 0
        }

        @Test
        fun `should black pawn promote to a Knight when possible`() {
            // Given When
            val game = givenAChessGame(fenPiecePlacementOnly = "4k3/P7/8/8/8/8/1p6/4K3", sideToMove = BLACK).applyMove(
                MoveCommand(B2, B1, KNIGHT)
            ).orThrow()
            // Then
            expectThat(game.chessboard.getPieceAt(B1)) isEqualTo BLACK_KNIGHT
            expectThat(game.chessboard.count { it.value == BLACK_PAWN }) isEqualTo 0
        }

        @Test
        fun `should black pawn promote to a Bishop when possible`() {
            // Given When
            val game = givenAChessGame(fenPiecePlacementOnly = "4k3/P7/8/8/8/8/1p6/4K3", sideToMove = BLACK).applyMove(
                MoveCommand(B2, B1, BISHOP)
            ).orThrow()
            // Then
            expectThat(game.chessboard.getPieceAt(B1)) isEqualTo BLACK_BISHOP
            expectThat(game.chessboard.count { it.value == BLACK_PAWN }) isEqualTo 0
        }

        @Test
        fun `should black pawn promote to a Rook when possible`() {
            // Given When
            val game = givenAChessGame(fenPiecePlacementOnly = "4k3/P7/8/8/8/8/1p6/4K3", sideToMove = BLACK).applyMove(
                MoveCommand(B2, B1, ROOK)
            ).orThrow()
            // Then
            expectThat(game.chessboard.getPieceAt(B1)) isEqualTo BLACK_ROOK
            expectThat(game.chessboard.count { it.value == BLACK_PAWN }) isEqualTo 0
        }


        @Test
        fun `should white pawn promote to a Queen when possible`() {
            // Given When
            val game = givenAChessGame(fenPiecePlacementOnly = "4k3/P7/8/8/8/8/1p6/4K3", sideToMove = WHITE).applyMove(
                MoveCommand(A7, A8, QUEEN)
            ).orThrow()
            // Then
            expectThat(game.chessboard.getPieceAt(A8)) isEqualTo WHITE_QUEEN
            expectThat(game.chessboard.count { it.value == WHITE_PAWN }) isEqualTo 0
        }

        @Test
        fun `should white pawn promote to a Knight when possible`() {
            // Given When
            val game = givenAChessGame(fenPiecePlacementOnly = "4k3/P7/8/8/8/8/1p6/4K3", sideToMove = WHITE).applyMove(
                MoveCommand(A7, A8, KNIGHT)
            ).orThrow()
            // Then
            expectThat(game.chessboard.getPieceAt(A8)) isEqualTo WHITE_KNIGHT
            expectThat(game.chessboard.count { it.value == WHITE_PAWN }) isEqualTo 0
        }

        @Test
        fun `should white pawn promote to a Bishop when possible`() {
            // Given When
            val game = givenAChessGame(fenPiecePlacementOnly = "4k3/P7/8/8/8/8/1p6/4K3", sideToMove = WHITE).applyMove(
                MoveCommand(A7, A8, BISHOP)
            ).orThrow()
            // Then
            expectThat(game.chessboard.getPieceAt(A8)) isEqualTo WHITE_BISHOP
            expectThat(game.chessboard.count { it.value == WHITE_PAWN }) isEqualTo 0
        }

        @Test
        fun `should white pawn promote to a Rook when possible`() {
            // Given When
            val game = givenAChessGame(fenPiecePlacementOnly = "4k3/P7/8/8/8/8/1p6/4K3", sideToMove = WHITE).applyMove(
                MoveCommand(A7, A8, ROOK)
            ).orThrow()
            // Then
            expectThat(game.chessboard.getPieceAt(A8)) isEqualTo WHITE_ROOK
            expectThat(game.chessboard.count { it.value == WHITE_PAWN }) isEqualTo 0
        }
    }

    @Nested
    inner class CapturedPiece {

        @Test
        fun `should remove piece from chessboard after capture`() {
            // Given
            val game = givenAChessGame(
                fenPiecePlacementOnly = "rnbqkbnr/pppp1ppp/8/4p3/3P4/8/PPP1PPPP/RNBQKBNR", sideToMove = WHITE
            )
            val initialAmountOfPieces = game.chessboard.numberOfRemainingPieces()
            // When
            val updatedGame = game.applyMove(MoveCommand(D4, E5)).orThrow()
            // Then
            expectThat(updatedGame.chessboard.getPieceAt(D4)).isNull()
            expectThat(updatedGame.chessboard.getPieceAt(E5)) isEqualTo WHITE_PAWN
            expectThat(updatedGame.chessboard.numberOfRemainingPieces()) isEqualTo initialAmountOfPieces - 1
        }

    }

    @Nested
    inner class Draw {

        @Test
        // https://www.chessprogramming.org/Fifty-move_Rule
        fun `should mark game outcome as draw regarding the 50 moves rule`() {
            // Given When
            val game = givenAChessGame(
                fenPiecePlacementOnly = "r7/4k3/2p5/8/8/2P5/4K3/7R",
                history = History(
                    halfMoveClock = 49,
                ),
                sideToMove = BLACK,
            ).applyMove(MoveCommand(A8, A1)).orThrow()
            // Then
            expectThat(game.status) isEqualTo DRAW
        }

        @Test
        // https://www.chessprogramming.org/Repetitions
        fun `should mark game outcome as draw when game history has a threefold repetition situation`() {
            // Given When
            val game = givenAChessGame(
                fenPiecePlacementOnly = "6r1/4k3/2p5/8/8/2P5/4K3/7R",
                sideToMove = WHITE,
            ).applyMoves(
                // When First Repetition
                MoveCommand(H1, G1),
                MoveCommand(G8, H8),
                MoveCommand(G1, H1),
                MoveCommand(H8, G8),
                // When Second Repetition
                MoveCommand(H1, G1),
                MoveCommand(G8, H8),
                MoveCommand(G1, H1),
                MoveCommand(H8, G8),
                // When Third Repetition
                MoveCommand(H1, G1),
                MoveCommand(G8, H8),
                MoveCommand(G1, H1),
                MoveCommand(H8, G8),
            ).orThrow()
            // Then
            expectThat(game.status) isEqualTo DRAW
        }

        @Nested
        // https://www.chessprogramming.org/Stalemate
        inner class Stalemate {

            @Test
            fun `should mark game outcome as draw with a simple stalemate`() {
                // Given When
                val game = givenAChessGame(
                    fenPiecePlacementOnly = "7k/4Q3/8/8/8/8/8/K7",
                    sideToMove = WHITE,
                ).applyMove(MoveCommand(E7, F7)).orThrow()
                // Then
                expectThat(game.status) isEqualTo DRAW
            }

            @Test
            fun `should mark game outcome as draw with a simple stalemate part 2`() {
                // Given When
                val game = givenAChessGame(
                    fenPiecePlacementOnly = "8/8/8/8/k7/2q5/8/1K6",
                    sideToMove = BLACK,
                ).applyMove(MoveCommand(A4, A3)).orThrow()
                // Then
                expectThat(game.status) isEqualTo DRAW
            }

            @Test
            fun `should mark game outcome as draw with a more complex stalemate (pinned king)`() {
                // Given When
                val game = givenAChessGame(
                    fenPiecePlacementOnly = "k7/7R/8/7p/5p1P/b7/8/RQ2N2K",
                    sideToMove = WHITE,
                ).applyMove(MoveCommand(E1, F3)).orThrow()
                // Then
                expectThat(game.status) isEqualTo DRAW
            }
        }

    }


    @Nested
    inner class FullMoveCounter {

        @Test
        fun `should test updating full move counter in game after white move`() {
            // Given When
            val game = givenAChessGame().applyMoves(
                MoveCommand(E2, E4),
                MoveCommand(E7, E5),
                MoveCommand(F1, C4),
                MoveCommand(B8, C6),
                MoveCommand(D1, H5),
            ).orThrow()
            // Then
            expectThat(game.history.fullMoveCounter) isEqualTo 3
        }

        @Test
        fun `should test updating full move counter in game after black move`() {
            // Given When
            val game = givenAChessGame().applyMoves(
                MoveCommand(E2, E4),
                MoveCommand(E7, E5),
                MoveCommand(F1, C4),
                MoveCommand(B8, C6),
                MoveCommand(D1, H5),
                MoveCommand(G8, F6),
            ).orThrow()
            // Then
            expectThat(game.history.fullMoveCounter) isEqualTo 4
        }
    }

    @Nested
    inner class HalfClockMove {
        @Test
        fun `should test increment of half clock move in game after not a pawn move`() {
            // Given When
            val game = givenAChessGame(
                fenPiecePlacementOnly = "4k2r/8/2p5/8/8/3P4/2K5/R7",
                history = History(
                    halfMoveClock = 10,
                ),
                sideToMove = WHITE,
            ).applyMove(MoveCommand(A1, A2)).orThrow()
            // Then
            expectThat(game.history.halfMoveClock) isEqualTo 11
        }

        @Test
        fun `should test rest half clock move in game after pawn move`() {
            // Given When
            val game = givenAChessGame(
                fenPiecePlacementOnly = "4k2r/8/2p5/8/8/3P4/2K5/R7",
                history = History(
                    halfMoveClock = 10,
                ),
                sideToMove = WHITE,
            ).applyMove(MoveCommand(D3, D4)).orThrow()
            // Then
            expectThat(game.history.halfMoveClock) isEqualTo 0
        }

        @Test
        fun `should test rest half clock move in game after captured piece`() {
            // Given When
            val game = givenAChessGame(
                fenPiecePlacementOnly = "4k2r/8/2p4P/8/8/3P4/2K5/R7",
                history = History(
                    halfMoveClock = 10,
                ),
                sideToMove = BLACK,
            ).applyMove(MoveCommand(H8, H6)).orThrow()
            // Then
            expectThat(game.history.halfMoveClock) isEqualTo 0
        }
    }


    @Nested
    inner class EnPassant {

        @Test
        fun `should change en passant target square in game and set it back to null when necessary`() {
            var game = givenAChessGame().applyMove(MoveCommand(E2, E4)).orThrow()
            expectThat(game.enPassantTargetSquare) isEqualTo E3
            game = game.applyMove(MoveCommand(E7, E5)).orThrow()
            expectThat(game.enPassantTargetSquare) isEqualTo E6
            game = game.applyMove(MoveCommand(D2, D3)).orThrow()
            expectThat(game.enPassantTargetSquare).isNull()
        }

        @Test
        fun `should not set en passant state after two one square move for a specific pawn`() {
            // Given When
            val game = givenAChessGame().applyMoves(
                MoveCommand(E2, E3),
                MoveCommand(E7, E6),
                MoveCommand(E3, E4),
            ).orThrow()
            // Then
            expectThat(game.enPassantTargetSquare).isNull()
        }

        @Test
        fun `should capture en passant to the right for white`() {
            // Given When
            val game = givenAChessGame(
                fenPiecePlacementOnly = "3k4/8/8/4Pp2/8/8/8/2K5", sideToMove = WHITE, enPassantTargetSquare = F6
            ).applyMove(MoveCommand(E5, F6)).orThrow()
            // Then
            expectThat(game.enPassantTargetSquare).isNull()
            expectThat(game.chessboard.getPieceAt(F6)) isEqualTo WHITE_PAWN
            expectThat(game.chessboard.getPieceAt(F5)).isNull()
            expectThat(game.chessboard.getPieceAt(E5)).isNull()
            expectThat(game.chessboard.getPieceAt(E6)).isNull()
        }

        @Test
        fun `should capture en passant to the left for white`() {
            // Given When
            val game = givenAChessGame(
                fenPiecePlacementOnly = "3k4/8/8/6pP/8/8/8/2K5", sideToMove = WHITE, enPassantTargetSquare = G6
            ).applyMove(MoveCommand(H5, G6)).orThrow()
            // Then
            expectThat(game.enPassantTargetSquare).isNull()
            expectThat(game.chessboard.getPieceAt(G6)) isEqualTo WHITE_PAWN
            expectThat(game.chessboard.getPieceAt(G5)).isNull()
            expectThat(game.chessboard.getPieceAt(H5)).isNull()
            expectThat(game.chessboard.getPieceAt(H6)).isNull()
        }

        @Test
        fun `should capture en passant to the right for black`() {
            // Given When
            val game = givenAChessGame(
                fenPiecePlacementOnly = "3k4/8/8/8/2pP4/8/8/2K5", sideToMove = BLACK, enPassantTargetSquare = D3
            ).applyMove(MoveCommand(C4, D3)).orThrow()
            // Then
            expectThat(game.enPassantTargetSquare).isNull()
            expectThat(game.chessboard.getPieceAt(D3)) isEqualTo BLACK_PAWN
            expectThat(game.chessboard.getPieceAt(D4)).isNull()
            expectThat(game.chessboard.getPieceAt(C4)).isNull()
            expectThat(game.chessboard.getPieceAt(C3)).isNull()
        }

        @Test
        fun `should capture en passant to the left for black`() {
            // Given When
            val game = givenAChessGame(
                fenPiecePlacementOnly = "3k4/8/8/8/4Pp2/8/8/2K5", sideToMove = BLACK, enPassantTargetSquare = E3
            ).applyMove(MoveCommand(F4, E3)).orThrow()
            // Then
            expectThat(game.enPassantTargetSquare).isNull()
            expectThat(game.chessboard.getPieceAt(E3)) isEqualTo BLACK_PAWN
            expectThat(game.chessboard.getPieceAt(F3)).isNull()
            expectThat(game.chessboard.getPieceAt(F4)).isNull()
            expectThat(game.chessboard.getPieceAt(E4)).isNull()
        }

    }

    @Nested
    inner class Castle {

        @Test
        fun `should not be able to castle queen side if there is a piece on the b file`() {
            // Given
            val game = givenAChessGame("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/5Q1p/PPPBBPPP/RN2K2R w KQkq - 1 1")
            // When Then
            expectThrows<IllegalStateException> {
                game.applyMoves(MoveCommand(E1, C1)).orThrow()
            }
        }

        @Test
        fun `should not be able to castle if a pawn is threatening one of the king path squares`() {
            // Given
            val game = givenAChessGame("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 2")
            // When then
            expectThrows<IllegalStateException> {
                game.applyMoves(
                    MoveCommand(A2, A3),
                    MoveCommand(A6, E2),
                    MoveCommand(E1, G1),
                ).orThrow()
            }
        }

        @Test
        fun `should lose ability to black queen castle if a8 rook moves`() {
            // Given When
            val game = givenAChessGame(
                fenPiecePlacementOnly = "r3k2r/8/8/8/8/8/8/R3K2R",
                sideToMove = BLACK,
                isBlackKingCastlePossible = true,
                isBlackQueenCastlePossible = true,
                isWhiteQueenCastlePossible = true,
                isWhiteKingCastlePossible = true,
            ).applyMove(MoveCommand(A8, A7)).orThrow()
            // Then
            expectThat(game.castles.isBlackQueenCastlePossible).isFalse()
            expectThat(game.castles.isBlackKingCastlePossible).isTrue()
            expectThat(game.castles.isWhiteKingCastlePossible).isTrue()
            expectThat(game.castles.isWhiteQueenCastlePossible).isTrue()
        }

        @Test
        fun `should lose ability to black queen castle after a8 black rook captured`() {
            // Given When
            val game = givenAChessGame(
                fenPiecePlacementOnly = "r3k2r/8/8/3B4/8/8/3K4/8",
                sideToMove = WHITE,
                isBlackKingCastlePossible = true,
                isBlackQueenCastlePossible = true,
                isWhiteQueenCastlePossible = false,
                isWhiteKingCastlePossible = false,
            ).applyMove(MoveCommand(D5, A8)).orThrow()
            // Then
            expectThat(game.castles.isBlackQueenCastlePossible).isFalse()
            expectThat(game.castles.isBlackKingCastlePossible).isTrue()
            expectThat(game.castles.isWhiteKingCastlePossible).isFalse()
            expectThat(game.castles.isWhiteQueenCastlePossible).isFalse()
        }

        @Test
        fun `should lose ability to black king castle after h8 black rook captured`() {
            // Given When
            val game = givenAChessGame(
                fenPiecePlacementOnly = "r3k2r/8/8/4B3/8/8/3K4/8",
                sideToMove = WHITE,
                isBlackKingCastlePossible = true,
                isBlackQueenCastlePossible = true,
                isWhiteQueenCastlePossible = false,
                isWhiteKingCastlePossible = false,
            ).applyMove(MoveCommand(E5, H8)).orThrow()
            // Then
            expectThat(game.castles.isBlackQueenCastlePossible).isTrue()
            expectThat(game.castles.isBlackKingCastlePossible).isFalse()
            expectThat(game.castles.isWhiteKingCastlePossible).isFalse()
            expectThat(game.castles.isWhiteQueenCastlePossible).isFalse()
        }

        @Test
        fun `should lose ability to white queen castle after a1 black rook captured`() {
            // Given When
            val game = givenAChessGame(
                fenPiecePlacementOnly = "4k3/8/8/8/3b4/8/8/R3K2R",
                sideToMove = BLACK,
                isBlackKingCastlePossible = false,
                isBlackQueenCastlePossible = false,
                isWhiteQueenCastlePossible = true,
                isWhiteKingCastlePossible = true,
            ).applyMove(MoveCommand(D4, A1)).orThrow()
            // Then
            expectThat(game.castles.isBlackQueenCastlePossible).isFalse()
            expectThat(game.castles.isBlackKingCastlePossible).isFalse()
            expectThat(game.castles.isWhiteKingCastlePossible).isTrue()
            expectThat(game.castles.isWhiteQueenCastlePossible).isFalse()
        }

        @Test
        fun `should lose ability to white king castle after h1 black rook captured`() {
            // Given When
            val game = givenAChessGame(
                fenPiecePlacementOnly = "4k3/8/8/8/4b3/8/8/R3K2R",
                sideToMove = BLACK,
                isBlackKingCastlePossible = false,
                isBlackQueenCastlePossible = false,
                isWhiteQueenCastlePossible = true,
                isWhiteKingCastlePossible = true,
            ).applyMove(MoveCommand(E4, H1)).orThrow()
            // Then
            expectThat(game.castles.isBlackQueenCastlePossible).isFalse()
            expectThat(game.castles.isBlackKingCastlePossible).isFalse()
            expectThat(game.castles.isWhiteKingCastlePossible).isFalse()
            expectThat(game.castles.isWhiteQueenCastlePossible).isTrue()
        }

        @Test
        fun `should lose ability to black king castle if h8 rook moves`() {
            // Given When
            val game = givenAChessGame(
                fenPiecePlacementOnly = "r3k2r/8/8/8/8/8/8/R3K2R",
                sideToMove = BLACK,
                isBlackKingCastlePossible = true,
                isBlackQueenCastlePossible = true,
                isWhiteQueenCastlePossible = true,
                isWhiteKingCastlePossible = true,
            ).applyMove(MoveCommand(H8, H7)).orThrow()
            // Then
            expectThat(game.castles.isBlackQueenCastlePossible).isTrue()
            expectThat(game.castles.isBlackKingCastlePossible).isFalse()
            expectThat(game.castles.isWhiteKingCastlePossible).isTrue()
            expectThat(game.castles.isWhiteQueenCastlePossible).isTrue()
        }

        @Test
        fun `should lose ability to white queen castle if a1 rook moves`() {
            // Given When
            val game = givenAChessGame(
                fenPiecePlacementOnly = "r3k2r/8/8/8/8/8/8/R3K2R",
                sideToMove = WHITE,
                isBlackKingCastlePossible = true,
                isBlackQueenCastlePossible = true,
                isWhiteQueenCastlePossible = true,
                isWhiteKingCastlePossible = true,
            ).applyMove(MoveCommand(A1, A2)).orThrow()
            // Then
            expectThat(game.castles.isBlackQueenCastlePossible).isTrue()
            expectThat(game.castles.isBlackKingCastlePossible).isTrue()
            expectThat(game.castles.isWhiteKingCastlePossible).isTrue()
            expectThat(game.castles.isWhiteQueenCastlePossible).isFalse()
        }

        @Test
        fun `should lose ability to white king castle if h1 rook moves`() {
            // Given When
            val game = givenAChessGame(
                fenPiecePlacementOnly = "r3k2r/8/8/8/8/8/8/R3K2R",
                sideToMove = WHITE,
                isBlackKingCastlePossible = true,
                isBlackQueenCastlePossible = true,
                isWhiteQueenCastlePossible = true,
                isWhiteKingCastlePossible = true,
            ).applyMove(MoveCommand(H1, H2)).orThrow()
            // Then
            expectThat(game.castles.isBlackQueenCastlePossible).isTrue()
            expectThat(game.castles.isBlackKingCastlePossible).isTrue()
            expectThat(game.castles.isWhiteKingCastlePossible).isFalse()
            expectThat(game.castles.isWhiteQueenCastlePossible).isTrue()
        }

        @Test
        fun `should loose all abilities to white castle moves wen white king moves`() {
            // Given When
            val game = givenAChessGame(
                fenPiecePlacementOnly = "r3k2r/8/8/8/8/8/8/R3K2R",
                sideToMove = WHITE,
                isBlackKingCastlePossible = true,
                isBlackQueenCastlePossible = true,
                isWhiteQueenCastlePossible = true,
                isWhiteKingCastlePossible = true,
            ).applyMove(MoveCommand(E1, E2)).orThrow()
            // Then
            expectThat(game.castles.isBlackQueenCastlePossible).isTrue()
            expectThat(game.castles.isBlackKingCastlePossible).isTrue()
            expectThat(game.castles.isWhiteKingCastlePossible).isFalse()
            expectThat(game.castles.isWhiteQueenCastlePossible).isFalse()
        }

        @Test
        fun `should loose all black castle moves wen black king moves`() {
            // Given When
            val game = givenAChessGame(
                fenPiecePlacementOnly = "r3k2r/8/8/8/8/8/8/R3K2R",
                sideToMove = BLACK,
                isBlackKingCastlePossible = true,
                isBlackQueenCastlePossible = true,
                isWhiteQueenCastlePossible = true,
                isWhiteKingCastlePossible = true,
            ).applyMove(MoveCommand(E8, E7)).orThrow()
            // Then
            expectThat(game.castles.isBlackQueenCastlePossible).isFalse()
            expectThat(game.castles.isBlackKingCastlePossible).isFalse()
            expectThat(game.castles.isWhiteKingCastlePossible).isTrue()
            expectThat(game.castles.isWhiteQueenCastlePossible).isTrue()
        }

        @Test
        fun `should perform white king castle move when possible`() {
            // Given When
            val game = givenAChessGame(
                fenPiecePlacementOnly = "r3k2r/8/8/8/8/8/8/R3K2R",
                sideToMove = WHITE,
                isBlackKingCastlePossible = true,
                isBlackQueenCastlePossible = true,
                isWhiteQueenCastlePossible = true,
                isWhiteKingCastlePossible = true,
            ).applyMove(MoveCommand(E1, G1)).orThrow()
            // Then
            expectThat(game.castles.isBlackQueenCastlePossible).isTrue()
            expectThat(game.castles.isBlackKingCastlePossible).isTrue()
            expectThat(game.castles.isWhiteKingCastlePossible).isFalse()
            expectThat(game.castles.isWhiteQueenCastlePossible).isFalse()
            expectThat(game.chessboard.getPieceAt(H1)).isNull()
            expectThat(game.chessboard.getPieceAt(E1)).isNull()
            expectThat(game.chessboard.getPieceAt(G1)) isEqualTo WHITE_KING
            expectThat(game.chessboard.getPieceAt(F1)) isEqualTo WHITE_ROOK
        }

        @Test
        fun `should perform white queen castle move when possible`() {
            // Given When
            val game = givenAChessGame(
                fenPiecePlacementOnly = "r3k2r/8/8/8/8/8/8/R3K2R",
                sideToMove = WHITE,
                isBlackKingCastlePossible = true,
                isBlackQueenCastlePossible = true,
                isWhiteQueenCastlePossible = true,
                isWhiteKingCastlePossible = true,
            ).applyMove(MoveCommand(E1, C1)).orThrow()
            // Then
            expectThat(game.castles.isBlackQueenCastlePossible).isTrue()
            expectThat(game.castles.isBlackKingCastlePossible).isTrue()
            expectThat(game.castles.isWhiteKingCastlePossible).isFalse()
            expectThat(game.castles.isWhiteQueenCastlePossible).isFalse()
            expectThat(game.chessboard.getPieceAt(A1)).isNull()
            expectThat(game.chessboard.getPieceAt(E1)).isNull()
            expectThat(game.chessboard.getPieceAt(C1)) isEqualTo WHITE_KING
            expectThat(game.chessboard.getPieceAt(D1)) isEqualTo WHITE_ROOK
        }

        @Test
        fun `should perform black king castle move when possible`() {
            // Given When
            val game = givenAChessGame(
                fenPiecePlacementOnly = "r3k2r/8/8/8/8/8/8/R3K2R",
                sideToMove = BLACK,
                isBlackKingCastlePossible = true,
                isBlackQueenCastlePossible = true,
                isWhiteQueenCastlePossible = true,
                isWhiteKingCastlePossible = true,
            ).applyMove(MoveCommand(E8, G8)).orThrow()
            // Then
            expectThat(game.castles.isBlackQueenCastlePossible).isFalse()
            expectThat(game.castles.isBlackKingCastlePossible).isFalse()
            expectThat(game.castles.isWhiteKingCastlePossible).isTrue()
            expectThat(game.castles.isWhiteQueenCastlePossible).isTrue()
            expectThat(game.chessboard.getPieceAt(H8)).isNull()
            expectThat(game.chessboard.getPieceAt(E8)).isNull()
            expectThat(game.chessboard.getPieceAt(G8)) isEqualTo BLACK_KING
            expectThat(game.chessboard.getPieceAt(F8)) isEqualTo BLACK_ROOK
        }

        @Test
        fun `should perform black queen castle move when possible`() {
            // Given When
            val game = givenAChessGame(
                fenPiecePlacementOnly = "r3k2r/8/8/8/8/8/8/R3K2R",
                isBlackKingCastlePossible = true,
                isBlackQueenCastlePossible = true,
                isWhiteQueenCastlePossible = true,
                isWhiteKingCastlePossible = true,
                sideToMove = BLACK
            ).applyMove(MoveCommand(E8, C8)).orThrow()
            // Then
            expectThat(game.castles.isBlackQueenCastlePossible).isFalse()
            expectThat(game.castles.isBlackKingCastlePossible).isFalse()
            expectThat(game.castles.isWhiteKingCastlePossible).isTrue()
            expectThat(game.castles.isWhiteQueenCastlePossible).isTrue()
            expectThat(game.chessboard.getPieceAt(A8)).isNull()
            expectThat(game.chessboard.getPieceAt(E8)).isNull()
            expectThat(game.chessboard.getPieceAt(C8)) isEqualTo BLACK_KING
            expectThat(game.chessboard.getPieceAt(D8)) isEqualTo BLACK_ROOK
        }

        @Test
        fun `should throw an error when attempting to castle and king moving squares are under attack`() {
            // Given
            val game = givenAChessGame(
                fenPiecePlacementOnly = "r3k2r/8/8/3R4/8/8/8/4K2R",
                isBlackKingCastlePossible = true,
                isBlackQueenCastlePossible = true,
                isWhiteQueenCastlePossible = true,
                isWhiteKingCastlePossible = true,
                sideToMove = BLACK
            )
            //  Then
            expectThat(game.applyMove(MoveCommand(E8, C8))).isA<Failure<MoveCommandError>>()
        }

        @Test
        fun `should throw an error when attempting to castle while being checked`() {
            // Given
            val game = givenAChessGame(
                fenPiecePlacementOnly = "r3k2r/8/8/4R3/8/8/8/4K2R",
                isBlackKingCastlePossible = true,
                isBlackQueenCastlePossible = true,
                isWhiteQueenCastlePossible = true,
                isWhiteKingCastlePossible = true,
                sideToMove = BLACK
            )
            // When Then
            expectThat(game.applyMove(MoveCommand(E8, C8))).isA<Failure<MoveCommandError>>()
            expectThat(game.applyMove(MoveCommand(E8, G8))).isA<Failure<MoveCommandError>>()
        }

        @Test
        fun `should not be able to castle while being checked and king castle squares are under attack`() {
            // Given
            val game = givenAChessGame("r3k2r/8/8/4Q3/8/8/8/4K3 w kq - 0 1")
            // When Then
            expectThat(game.applyMove(MoveCommand(E8, C8))).isA<Failure<MoveCommandError>>()
            expectThat(game.applyMove(MoveCommand(E8, G8))).isA<Failure<MoveCommandError>>()
        }


    }

    @Nested
    inner class Check {
        @Test
        fun `should mark move as check when checking king`() {
            // Given When
            val game = givenAChessGame(
                fenPiecePlacementOnly = "2k5/8/8/8/8/8/3Q4/2K5",
                sideToMove = WHITE
            ).applyMove(MoveCommand(origin = D2, destination = C2)).orThrow()
            // Then
            expectThat(game.history.moves.last().isCheck).isTrue()
            expectThat(game.history.moves.size) isEqualTo 1
        }

        @Test
        fun `should mark move as check on promotion when checking king`() {
            // Given When
            val game = givenAChessGame(
                fenPiecePlacementOnly = "8/6P1/8/8/2k5/8/8/2K5",
                sideToMove = WHITE
            ).applyMove(MoveCommand(origin = G7, destination = G8, promotedPiece = BISHOP)).orThrow()
            // Then
            expectThat(game.history.moves.last().isCheck).isTrue()
        }

        @Test
        fun `should not mark move as check on normal move`() {
            // Given When
            val game = givenAChessGame().applyMove(MoveCommand(origin = E2, destination = E4)).orThrow()
            // Then
            expectThat(game.history.moves.last().isCheck).isFalse()
        }
    }
}

