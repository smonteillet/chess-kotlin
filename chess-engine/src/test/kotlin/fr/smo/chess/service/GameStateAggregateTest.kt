package fr.smo.chess.service

import fr.smo.chess.fixtures.GameStateFixtures.Companion.givenAChessGame
import fr.smo.chess.model.Color.BLACK
import fr.smo.chess.model.Color.WHITE
import fr.smo.chess.model.GameState
import fr.smo.chess.model.GameState.GameOutcome.*
import fr.smo.chess.model.MoveRequest
import fr.smo.chess.model.Piece.*
import fr.smo.chess.model.PieceType.*
import fr.smo.chess.model.Square.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull
import strikt.assertions.isTrue

class GameStateAggregateTest {

    private val gameStateAggregate = GameStateAggregate()

    @Nested
    inner class CheckMate {
        @Test
        fun `should mark game as won when performing Scholar's mate`() {
            // Given
            var gameState = GameState.NEW_STANDARD_CHESS_GAME
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(E2,E4), gameState)
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(E7,E5), gameState)
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(F1,C4), gameState)
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(B8,C6), gameState)
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(D1,H5), gameState)
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(G8,F6), gameState)
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(H5,F7), gameState)
            // Then
            expectThat(gameState.gameOutcome) isEqualTo WHITE_WIN
        }

        @Test
        fun `should mark game as won when performing back rank mate`() {
            // Given
            var gameState = givenAChessGame(fenPiecePlacementOnly = "4k2r/p7/8/8/8/8/PPP5/1K6", sideToMove = BLACK)
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(H8,H1), gameState)
            // Then
            expectThat(gameState.gameOutcome) isEqualTo BLACK_WIN
        }

        @Test
        fun `should mark game as won when performing kiss of death checkmate`() {
            // Given
            var gameState = givenAChessGame(fenPiecePlacementOnly = "8/8/8/7P/8/1k6/4q3/1K6", sideToMove = BLACK)
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(E2,B2), gameState)
            // Then
            expectThat(gameState.gameOutcome) isEqualTo BLACK_WIN
        }

        @Test
        fun `should mark game as won when performing smothered mate checkmate`() {
            // Given
            var gameState = givenAChessGame(fenPiecePlacementOnly = "6rk/6pp/2PN4/8/8/8/8/1K6", sideToMove = WHITE)
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(D6,F7), gameState)
            // Then
            expectThat(gameState.gameOutcome) isEqualTo WHITE_WIN
        }

        @Test
        fun `should mark game as won when performing two rook checkmate`() {
            // Given
            var gameState = givenAChessGame(fenPiecePlacementOnly = "8/3R4/8/7k/8/1p4R1/8/3K4", sideToMove = WHITE)
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(D7,H7), gameState)
            // Then
            expectThat(gameState.gameOutcome) isEqualTo WHITE_WIN
        }

        @Test
        fun `should mark game as won when performing rook + king checkmate`() {
            // Given
            var gameState = givenAChessGame(fenPiecePlacementOnly = "8/8/8/8/8/2k5/7r/2K5", sideToMove = BLACK)
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(H2,H1), gameState)
            // Then
            expectThat(gameState.gameOutcome) isEqualTo BLACK_WIN
        }

        @Test
        fun `should mark game as won after a promotion checkmate`() {
            // Given
            var gameState = givenAChessGame(fenPiecePlacementOnly = "8/8/8/8/8/2k5/7p/2K5", sideToMove = BLACK)
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(H2,H1, QUEEN), gameState)
            // Then
            expectThat(gameState.gameOutcome) isEqualTo BLACK_WIN
        }
    }

    @Nested
    inner class CasualMove {

        @Test
        fun `should pawn moved without capture lead to no pieces amount changed on chessboard`() {
            // Given
            var gameState = GameState.NEW_STANDARD_CHESS_GAME
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(H2,H4), gameState)
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(H7, H5 ),gameState)
            // Then
            expectThat(gameState.moveHistory.none { it.capturedPiece != null })
            expectThat(gameState.chessboard.piecesOnBoard.size) isEqualTo 32
        }
    }

    @Nested
    inner class CheckSituation {

        @Test
        fun `should throw an error when performing a move that doesn't revert check when king is checked`() {
            // Given
            var gameState = givenAChessGame(fenPiecePlacementOnly = "3k4/1P6/8/8/8/8/7r/2K5", sideToMove = BLACK)
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(H2,H1), gameState) // White King check

            // Then
            assertThrows<IllegalStateException> {
                gameStateAggregate.applyMoveRequest(MoveRequest(B7,B8), gameState)
            }
        }

        @Test
        fun `should throw an error when performing a move that doesn't revert check when king is checked after promotion`() {
            // Given
            var gameState = givenAChessGame(fenPiecePlacementOnly = "3k4/1P6/8/8/8/8/7p/2K5", sideToMove = BLACK)
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(H2,H1, QUEEN), gameState) // White King check

            // Then
            assertThrows<IllegalStateException> {
                gameStateAggregate.applyMoveRequest(MoveRequest(B7,B8), gameState)
            }
        }

    }

    @Nested
    inner class Promotion {
        @Test
        fun `should black pawn promote to a Queen when possible`() {
            // Given
            var gameState = givenAChessGame(fenPiecePlacementOnly = "4k3/P7/8/8/8/8/1p6/4K3", sideToMove = BLACK)
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(B2,B1, QUEEN), gameState)
            // Then
            expectThat(gameState.chessboard.getPositionAt(B1)?.piece) isEqualTo  BLACK_QUEEN
            expectThat(gameState.chessboard.piecesOnBoard.count { it.piece == BLACK_PAWN }) isEqualTo  0
        }

        @Test
        fun `should black pawn promote to a Knight when possible`() {
            // Given
            var gameState = givenAChessGame(fenPiecePlacementOnly = "4k3/P7/8/8/8/8/1p6/4K3", sideToMove = BLACK)
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(B2,B1, KNIGHT), gameState)
            // Then
            expectThat(gameState.chessboard.getPositionAt(B1)?.piece) isEqualTo BLACK_KNIGHT
            expectThat(gameState.chessboard.piecesOnBoard.count { it.piece == BLACK_PAWN }) isEqualTo  0
        }

        @Test
        fun `should black pawn promote to a Bishop when possible`() {
            // Given
            var gameState = givenAChessGame(fenPiecePlacementOnly = "4k3/P7/8/8/8/8/1p6/4K3", sideToMove = BLACK)
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(B2,B1, BISHOP), gameState)
            // Then
            expectThat(gameState.chessboard.getPositionAt(B1)?.piece) isEqualTo  BLACK_BISHOP
            expectThat(gameState.chessboard.piecesOnBoard.count { it.piece == BLACK_PAWN }) isEqualTo  0
        }

        @Test
        fun `should black pawn promote to a Rook when possible`() {
            // Given
            var gameState = givenAChessGame(fenPiecePlacementOnly = "4k3/P7/8/8/8/8/1p6/4K3", sideToMove = BLACK)
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(B2,B1, ROOK), gameState)
            // Then
            expectThat(gameState.chessboard.getPositionAt(B1)?.piece) isEqualTo  BLACK_ROOK
            expectThat(gameState.chessboard.piecesOnBoard.count { it.piece == BLACK_PAWN }) isEqualTo  0
        }



        @Test
        fun `should white pawn promote to a Queen when possible`() {
            // Given
            var gameState = givenAChessGame(fenPiecePlacementOnly = "4k3/P7/8/8/8/8/1p6/4K3", sideToMove = WHITE)
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(A7,A8, QUEEN), gameState)
            // Then
            expectThat(gameState.chessboard.getPositionAt(A8)?.piece) isEqualTo  WHITE_QUEEN
            expectThat(gameState.chessboard.piecesOnBoard.count { it.piece == WHITE_PAWN }) isEqualTo  0
        }

        @Test
        fun `should white pawn promote to a Knight when possible`() {
            // Given
            var gameState = givenAChessGame(fenPiecePlacementOnly = "4k3/P7/8/8/8/8/1p6/4K3", sideToMove = WHITE)
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(A7,A8, KNIGHT), gameState)
            // Then
            expectThat(gameState.chessboard.getPositionAt(A8)?.piece) isEqualTo  WHITE_KNIGHT
            expectThat(gameState.chessboard.piecesOnBoard.count { it.piece == WHITE_PAWN }) isEqualTo  0
        }

        @Test
        fun `should white pawn promote to a Bishop when possible`() {
            // Given
            var gameState = givenAChessGame(fenPiecePlacementOnly = "4k3/P7/8/8/8/8/1p6/4K3", sideToMove = WHITE)
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(A7,A8, BISHOP), gameState)
            // Then
            expectThat(gameState.chessboard.getPositionAt(A8)?.piece) isEqualTo  WHITE_BISHOP
            expectThat(gameState.chessboard.piecesOnBoard.count { it.piece == WHITE_PAWN }) isEqualTo  0
        }

        @Test
        fun `should white pawn promote to a Rook when possible`() {
            // Given
            var gameState = givenAChessGame(fenPiecePlacementOnly = "4k3/P7/8/8/8/8/1p6/4K3", sideToMove = WHITE)
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(A7,A8, ROOK), gameState)
            // Then
            expectThat(gameState.chessboard.getPositionAt(A8)?.piece) isEqualTo  WHITE_ROOK
            expectThat(gameState.chessboard.piecesOnBoard.count { it.piece == WHITE_PAWN }) isEqualTo  0
        }
    }

    @Nested
    inner class CapturedPiece {

        @Test
        fun `should remove piece from chessboard after capture`() {
            // Given
            var gameState = givenAChessGame(
                fenPiecePlacementOnly = "rnbqkbnr/pppp1ppp/8/4p3/3P4/8/PPP1PPPP/RNBQKBNR",
                sideToMove = WHITE
            )
            val initialAmountOfPieces = gameState.chessboard.piecesOnBoard.size
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(D4,E5), gameState)
            // Then
            expectThat(gameState.chessboard.getPositionAt(D4)).isNull()
            expectThat(gameState.chessboard.getPositionAt(E5)?.piece) isEqualTo WHITE_PAWN
            expectThat(gameState.chessboard.piecesOnBoard.size) isEqualTo initialAmountOfPieces - 1
        }

    }

    @Nested
    inner class Draw {

        @Test
        // https://www.chessprogramming.org/Fifty-move_Rule
        fun `should mark game outcome as draw regarding the 50 moves rule`() {
            // Given
            var gameState = givenAChessGame(
                fenPiecePlacementOnly = "r7/4k3/2p5/8/8/2P5/4K3/7R",
                halfMoveClock = 49,
                sideToMove = BLACK,
            )
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(A8,A1), gameState)
            // Then
            expectThat(gameState.gameOutcome) isEqualTo DRAW
        }

        @Test
        // https://www.chessprogramming.org/Repetitions
        fun `should mark game outcome as draw when game history has a threefold repetition situation`() {
            // Given
            var gameState = givenAChessGame(
                fenPiecePlacementOnly = "6r1/4k3/2p5/8/8/2P5/4K3/7R",
                sideToMove = WHITE,
            )
            // When First Repetition
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(H1,G1), gameState)
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(G8,H8), gameState)
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(G1,H1), gameState)
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(H8,G8), gameState)
            // When Second Repetition
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(H1,G1), gameState)
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(G8,H8), gameState)
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(G1,H1), gameState)
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(H8,G8), gameState)
            // When Third Repetition
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(H1,G1), gameState)
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(G8,H8), gameState)
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(G1,H1), gameState)
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(H8,G8), gameState)
            // Then
            expectThat(gameState.gameOutcome) isEqualTo DRAW
        }

        @Nested
        // https://www.chessprogramming.org/Stalemate
        inner class Stalemate {

            @Test
            fun `should mark game outcome as draw with a simple stalemate`() {
                // Given
                var gameState = givenAChessGame(
                    fenPiecePlacementOnly = "7k/4Q3/8/8/8/8/8/K7",
                    sideToMove = WHITE,
                )
                // when
                gameState = gameStateAggregate.applyMoveRequest(MoveRequest(E7,F7), gameState)
                // Then
                expectThat(gameState.gameOutcome) isEqualTo DRAW
            }

            @Test
            fun `should mark game outcome as draw with a simple stalemate part 2`() {
                // Given
                var gameState = givenAChessGame(
                    fenPiecePlacementOnly = "8/8/8/8/k7/2q5/8/1K6",
                    sideToMove = BLACK,
                )
                // when
                gameState = gameStateAggregate.applyMoveRequest(MoveRequest(A4,A3), gameState)
                // Then
                expectThat(gameState.gameOutcome) isEqualTo DRAW
            }

            @Test
            fun `should mark game outcome as draw with a more complex stalemate (pinned king)`() {
                // Given
                var gameState = givenAChessGame(
                    fenPiecePlacementOnly = "k7/7R/8/7p/5p1P/b7/8/RQ2N2K",
                    sideToMove = WHITE,
                )
                // when
                gameState = gameStateAggregate.applyMoveRequest(MoveRequest(E1,F3), gameState)
                // Then
                expectThat(gameState.gameOutcome) isEqualTo DRAW
            }
        }

    }


    @Nested
    inner class FullMoveCounter {

        @Test
        fun `should test updating full move counter in game state after white move`() {
            // Given
            var gameState =  GameState.StartingPositions.NEW_STANDARD_CHESS_GAME
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(E2,E4), gameState)
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(E7,E5), gameState)
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(F1,C4), gameState)
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(B8,C6), gameState)
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(D1,H5), gameState)
            // Then
            expectThat(gameState.fullMoveCounter) isEqualTo 3
        }

        @Test
        fun `should test updating full move counter in game state after black move`() {
            // Given
            var gameState = GameState.StartingPositions.NEW_STANDARD_CHESS_GAME
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(E2,E4), gameState)
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(E7,E5), gameState)
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(F1,C4), gameState)
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(B8,C6), gameState)
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(D1,H5), gameState)
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(G8,F6), gameState)
            // Then
            expectThat(gameState.fullMoveCounter) isEqualTo 4
        }
    }

    @Nested
    inner class HalfClockMove {
        @Test
        fun `should test increment of half clock move in game state after not a pawn move`() {
            // Given
            var gameState = givenAChessGame(
                fenPiecePlacementOnly = "4k2r/8/2p5/8/8/3P4/2K5/R7",
                halfMoveClock = 10,
                sideToMove = WHITE,
            )
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(A1,A2), gameState)
            // Then
            expectThat(gameState.halfMoveClock) isEqualTo 11
        }

        @Test
        fun `should test rest half clock move in game state after pawn move`() {
            // Given
            var gameState = givenAChessGame(
                fenPiecePlacementOnly = "4k2r/8/2p5/8/8/3P4/2K5/R7",
                halfMoveClock = 10,
                sideToMove = WHITE,
            )
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(D3,D4), gameState)
            // Then
            expectThat(gameState.halfMoveClock) isEqualTo 0
        }

        @Test
        fun `should test rest half clock move in game state after captured piece`() {
            // Given
            var gameState = givenAChessGame(
                fenPiecePlacementOnly = "4k2r/8/2p4P/8/8/3P4/2K5/R7",
                halfMoveClock = 10,
                sideToMove = BLACK,
            )
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(H8,H6), gameState)
            // Then
            expectThat(gameState.halfMoveClock) isEqualTo 0
        }
    }


    @Nested
    inner class EnPassant {

        @Test
        fun `should change en passant target square in game state and set it back to null when necessary`() {
            var gameState = GameState.StartingPositions.NEW_STANDARD_CHESS_GAME
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(E2, E4), gameState)
            expectThat(gameState.enPassantTargetSquare) isEqualTo E3
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(E7, E5), gameState)
            expectThat(gameState.enPassantTargetSquare) isEqualTo E6
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(D2, D3), gameState)
            expectThat(gameState.enPassantTargetSquare).isNull()
        }

        @Test
        fun `should capture en passant to the right for white`() {
            // Given
            var gameState = givenAChessGame(fenPiecePlacementOnly = "3k4/8/8/4Pp2/8/8/8/2K5", sideToMove = WHITE,
                enPassantTargetSquare = F6)
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(E5,F6), gameState)
            // Then
            expectThat(gameState.enPassantTargetSquare).isNull()
            expectThat(gameState.chessboard.getPositionAt(F6)?.piece) isEqualTo WHITE_PAWN
            expectThat(gameState.chessboard.getPositionAt(F5)).isNull()
            expectThat(gameState.chessboard.getPositionAt(E5)).isNull()
            expectThat(gameState.chessboard.getPositionAt(E6)).isNull()
        }

        @Test
        fun `should capture en passant to the left for white`() {
            // Given
            var gameState = givenAChessGame(fenPiecePlacementOnly = "3k4/8/8/6pP/8/8/8/2K5", sideToMove = WHITE,
                enPassantTargetSquare = G6)
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(H5,G6), gameState)
            // Then
            expectThat(gameState.enPassantTargetSquare).isNull()
            expectThat(gameState.chessboard.getPositionAt(G6)?.piece) isEqualTo WHITE_PAWN
            expectThat(gameState.chessboard.getPositionAt(G5)).isNull()
            expectThat(gameState.chessboard.getPositionAt(H5)).isNull()
            expectThat(gameState.chessboard.getPositionAt(H6)).isNull()
        }

        @Test
        fun `should capture en passant to the right for black`() {
            // Given
            var gameState = givenAChessGame(fenPiecePlacementOnly = "3k4/8/8/8/2pP4/8/8/2K5", sideToMove = BLACK,
                enPassantTargetSquare = D3)
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(C4,D3), gameState)
            // Then
            expectThat(gameState.enPassantTargetSquare).isNull()
            expectThat(gameState.chessboard.getPositionAt(D3)?.piece) isEqualTo BLACK_PAWN
            expectThat(gameState.chessboard.getPositionAt(D4)).isNull()
            expectThat(gameState.chessboard.getPositionAt(C4)).isNull()
            expectThat(gameState.chessboard.getPositionAt(C3)).isNull()
        }

        @Test
        fun `should capture en passant to the left for black`() {
            // Given
            var gameState = givenAChessGame(fenPiecePlacementOnly = "3k4/8/8/8/4Pp2/8/8/2K5", sideToMove = BLACK,
                enPassantTargetSquare = E3)
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(F4,E3), gameState)
            // Then
            expectThat(gameState.enPassantTargetSquare).isNull()
            expectThat(gameState.chessboard.getPositionAt(E3)?.piece) isEqualTo BLACK_PAWN
            expectThat(gameState.chessboard.getPositionAt(F3)).isNull()
            expectThat(gameState.chessboard.getPositionAt(F4)).isNull()
            expectThat(gameState.chessboard.getPositionAt(E4)).isNull()
        }
    }

    @Nested
    inner class Castle {

        @Test
        fun `should lose ability to black queen castle if a8 rook moves`() {
            // Given
            var gameState = givenAChessGame(
                fenPiecePlacementOnly = "r3k2r/8/8/8/8/8/8/R3K2R",
                sideToMove = BLACK,
                isBlackKingCastlePossible = true,
                isBlackQueenCastlePossible = true,
                isWhiteQueenCastlePossible = true,
                isWhiteKingCastlePossible = true,
            )
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(A8,A7), gameState)
            // Then
            expectThat(gameState.isBlackQueenCastlePossible).isFalse()
            expectThat(gameState.isBlackKingCastlePossible).isTrue()
            expectThat(gameState.isWhiteKingCastlePossible).isTrue()
            expectThat(gameState.isWhiteQueenCastlePossible).isTrue()
        }

        @Test
        fun `should lose ability to black queen castle after a8 black rook captured`() {
            // Given
            var gameState = givenAChessGame(
                fenPiecePlacementOnly = "r3k2r/8/8/3B4/8/8/3K4/8",
                sideToMove = WHITE,
                isBlackKingCastlePossible = true,
                isBlackQueenCastlePossible = true,
                isWhiteQueenCastlePossible = false,
                isWhiteKingCastlePossible = false,
            )
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(D5,A8), gameState)
            // Then
            expectThat(gameState.isBlackQueenCastlePossible).isFalse()
            expectThat(gameState.isBlackKingCastlePossible).isTrue()
            expectThat(gameState.isWhiteKingCastlePossible).isFalse()
            expectThat(gameState.isWhiteQueenCastlePossible).isFalse()
        }

        @Test
        fun `should lose ability to black king castle after h8 black rook captured`() {
            // Given
            var gameState = givenAChessGame(
                fenPiecePlacementOnly = "r3k2r/8/8/4B3/8/8/3K4/8",
                sideToMove = WHITE,
                isBlackKingCastlePossible = true,
                isBlackQueenCastlePossible = true,
                isWhiteQueenCastlePossible = false,
                isWhiteKingCastlePossible = false,
            )
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(E5,H8), gameState)
            // Then
            expectThat(gameState.isBlackQueenCastlePossible).isTrue()
            expectThat(gameState.isBlackKingCastlePossible).isFalse()
            expectThat(gameState.isWhiteKingCastlePossible).isFalse()
            expectThat(gameState.isWhiteQueenCastlePossible).isFalse()
        }

        @Test
        fun `should lose ability to white queen castle after a1 black rook captured`() {
            // Given
            var gameState = givenAChessGame(
                fenPiecePlacementOnly = "4k3/8/8/8/3b4/8/8/R3K2R",
                sideToMove = BLACK,
                isBlackKingCastlePossible = false,
                isBlackQueenCastlePossible = false,
                isWhiteQueenCastlePossible = true,
                isWhiteKingCastlePossible = true,
            )
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(D4,A1), gameState)
            // Then
            expectThat(gameState.isBlackQueenCastlePossible).isFalse()
            expectThat(gameState.isBlackKingCastlePossible).isFalse()
            expectThat(gameState.isWhiteKingCastlePossible).isTrue()
            expectThat(gameState.isWhiteQueenCastlePossible).isFalse()
        }

        @Test
        fun `should lose ability to white king castle after h1 black rook captured`() {
            // Given
            var gameState = givenAChessGame(
                fenPiecePlacementOnly = "4k3/8/8/8/4b3/8/8/R3K2R",
                sideToMove = BLACK,
                isBlackKingCastlePossible = false,
                isBlackQueenCastlePossible = false,
                isWhiteQueenCastlePossible = true,
                isWhiteKingCastlePossible = true,
            )
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(E4,H1), gameState)
            // Then
            expectThat(gameState.isBlackQueenCastlePossible).isFalse()
            expectThat(gameState.isBlackKingCastlePossible).isFalse()
            expectThat(gameState.isWhiteKingCastlePossible).isFalse()
            expectThat(gameState.isWhiteQueenCastlePossible).isTrue()
        }

        @Test
        fun `should lose ability to black king castle if h8 rook moves`() {
            // Given
            var gameState = givenAChessGame(
                fenPiecePlacementOnly = "r3k2r/8/8/8/8/8/8/R3K2R",
                sideToMove = BLACK,
                isBlackKingCastlePossible = true,
                isBlackQueenCastlePossible = true,
                isWhiteQueenCastlePossible = true,
                isWhiteKingCastlePossible = true,
            )
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(H8,H7), gameState)
            // Then
            expectThat(gameState.isBlackQueenCastlePossible).isTrue()
            expectThat(gameState.isBlackKingCastlePossible).isFalse()
            expectThat(gameState.isWhiteKingCastlePossible).isTrue()
            expectThat(gameState.isWhiteQueenCastlePossible).isTrue()
        }

        @Test
        fun `should lose ability to white queen castle if a1 rook moves`() {
            // Given
            var gameState = givenAChessGame(
                fenPiecePlacementOnly = "r3k2r/8/8/8/8/8/8/R3K2R",
                sideToMove = WHITE,
                isBlackKingCastlePossible = true,
                isBlackQueenCastlePossible = true,
                isWhiteQueenCastlePossible = true,
                isWhiteKingCastlePossible = true,
            )
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(A1,A2), gameState)
            // Then
            expectThat(gameState.isBlackQueenCastlePossible).isTrue()
            expectThat(gameState.isBlackKingCastlePossible).isTrue()
            expectThat(gameState.isWhiteKingCastlePossible).isTrue()
            expectThat(gameState.isWhiteQueenCastlePossible).isFalse()
        }

        @Test
        fun `should lose ability to white king castle if h1 rook moves`() {
            // Given
            var gameState = givenAChessGame(
                fenPiecePlacementOnly = "r3k2r/8/8/8/8/8/8/R3K2R",
                sideToMove = WHITE,
                isBlackKingCastlePossible = true,
                isBlackQueenCastlePossible = true,
                isWhiteQueenCastlePossible = true,
                isWhiteKingCastlePossible = true,
            )
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(H1,H2), gameState)
            // Then
            expectThat(gameState.isBlackQueenCastlePossible).isTrue()
            expectThat(gameState.isBlackKingCastlePossible).isTrue()
            expectThat(gameState.isWhiteKingCastlePossible).isFalse()
            expectThat(gameState.isWhiteQueenCastlePossible).isTrue()
        }

        @Test
        fun `should loose all abilities to white castle moves wen white king moves`() {
            // Given
            var gameState = givenAChessGame(
                fenPiecePlacementOnly = "r3k2r/8/8/8/8/8/8/R3K2R",
                sideToMove = WHITE,
                isBlackKingCastlePossible = true,
                isBlackQueenCastlePossible = true,
                isWhiteQueenCastlePossible = true,
                isWhiteKingCastlePossible = true,
            )
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(E1,E2), gameState)
            // Then
            expectThat(gameState.isBlackQueenCastlePossible).isTrue()
            expectThat(gameState.isBlackKingCastlePossible).isTrue()
            expectThat(gameState.isWhiteKingCastlePossible).isFalse()
            expectThat(gameState.isWhiteQueenCastlePossible).isFalse()
        }

        @Test
        fun `should loose all black castle moves wen black king moves`() {
            // Given
            var gameState = givenAChessGame(
                fenPiecePlacementOnly = "r3k2r/8/8/8/8/8/8/R3K2R",
                sideToMove = BLACK,
                isBlackKingCastlePossible = true,
                isBlackQueenCastlePossible = true,
                isWhiteQueenCastlePossible = true,
                isWhiteKingCastlePossible = true,
            )
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(E8,E7), gameState)
            // Then
            expectThat(gameState.isBlackQueenCastlePossible).isFalse()
            expectThat(gameState.isBlackKingCastlePossible).isFalse()
            expectThat(gameState.isWhiteKingCastlePossible).isTrue()
            expectThat(gameState.isWhiteQueenCastlePossible).isTrue()
        }

        @Test
        fun `should perform white king castle move when possible`() {
            // Given
            var gameState = givenAChessGame(
                fenPiecePlacementOnly = "r3k2r/8/8/8/8/8/8/R3K2R",
                sideToMove = WHITE,
                isBlackKingCastlePossible = true,
                isBlackQueenCastlePossible = true,
                isWhiteQueenCastlePossible = true,
                isWhiteKingCastlePossible = true,
            )
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(E1,G1), gameState)
            // Then
            expectThat(gameState.isBlackQueenCastlePossible).isTrue()
            expectThat(gameState.isBlackKingCastlePossible).isTrue()
            expectThat(gameState.isWhiteKingCastlePossible).isFalse()
            expectThat(gameState.isWhiteQueenCastlePossible).isFalse()
            expectThat(gameState.chessboard.getPositionAt(H1)).isNull()
            expectThat(gameState.chessboard.getPositionAt(E1)).isNull()
            expectThat(gameState.chessboard.getPositionAt(G1)?.piece) isEqualTo  WHITE_KING
            expectThat(gameState.chessboard.getPositionAt(F1)?.piece) isEqualTo  WHITE_ROOK
        }

        @Test
        fun `should perform white queen castle move when possible`() {
            // Given
            var gameState = givenAChessGame(
                fenPiecePlacementOnly = "r3k2r/8/8/8/8/8/8/R3K2R",
                sideToMove = WHITE,
                isBlackKingCastlePossible = true,
                isBlackQueenCastlePossible = true,
                isWhiteQueenCastlePossible = true,
                isWhiteKingCastlePossible = true,
            )
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(E1,C1), gameState)
            // Then
            expectThat(gameState.isBlackQueenCastlePossible).isTrue()
            expectThat(gameState.isBlackKingCastlePossible).isTrue()
            expectThat(gameState.isWhiteKingCastlePossible).isFalse()
            expectThat(gameState.isWhiteQueenCastlePossible).isFalse()
            expectThat(gameState.chessboard.getPositionAt(A1)).isNull()
            expectThat(gameState.chessboard.getPositionAt(E1)).isNull()
            expectThat(gameState.chessboard.getPositionAt(C1)?.piece) isEqualTo  WHITE_KING
            expectThat(gameState.chessboard.getPositionAt(D1)?.piece) isEqualTo  WHITE_ROOK
        }

        @Test
        fun `should perform black king castle move when possible`() {
            // Given
            var gameState = givenAChessGame(
                fenPiecePlacementOnly = "r3k2r/8/8/8/8/8/8/R3K2R",
                sideToMove = BLACK,
                isBlackKingCastlePossible = true,
                isBlackQueenCastlePossible = true,
                isWhiteQueenCastlePossible = true,
                isWhiteKingCastlePossible = true,
            )
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(E8,G8), gameState)
            // Then
            expectThat(gameState.isBlackQueenCastlePossible).isFalse()
            expectThat(gameState.isBlackKingCastlePossible).isFalse()
            expectThat(gameState.isWhiteKingCastlePossible).isTrue()
            expectThat(gameState.isWhiteQueenCastlePossible).isTrue()
            expectThat(gameState.chessboard.getPositionAt(H8)).isNull()
            expectThat(gameState.chessboard.getPositionAt(E8)).isNull()
            expectThat(gameState.chessboard.getPositionAt(G8)?.piece) isEqualTo  BLACK_KING
            expectThat(gameState.chessboard.getPositionAt(F8)?.piece) isEqualTo  BLACK_ROOK
        }

        @Test
        fun `should perform black queen castle move when possible`() {
            // Given
            var gameState = givenAChessGame(
                fenPiecePlacementOnly = "r3k2r/8/8/8/8/8/8/R3K2R",
                isBlackKingCastlePossible = true,
                isBlackQueenCastlePossible = true,
                isWhiteQueenCastlePossible = true,
                isWhiteKingCastlePossible = true,
                sideToMove = BLACK)
            // When
            gameState = gameStateAggregate.applyMoveRequest(MoveRequest(E8,C8), gameState)
            // Then
            expectThat(gameState.isBlackQueenCastlePossible).isFalse()
            expectThat(gameState.isBlackKingCastlePossible).isFalse()
            expectThat(gameState.isWhiteKingCastlePossible).isTrue()
            expectThat(gameState.isWhiteQueenCastlePossible).isTrue()
            expectThat(gameState.chessboard.getPositionAt(A8)).isNull()
            expectThat(gameState.chessboard.getPositionAt(E8)).isNull()
            expectThat(gameState.chessboard.getPositionAt(C8)?.piece) isEqualTo  BLACK_KING
            expectThat(gameState.chessboard.getPositionAt(D8)?.piece) isEqualTo  BLACK_ROOK
        }

        @Test
        fun `should throw an error when attempting to castle and king moving squares are under attack`() {
            // Given
            val gameState = givenAChessGame(
                fenPiecePlacementOnly = "r3k2r/8/8/3R4/8/8/8/4K2R",
                isBlackKingCastlePossible = true,
                isBlackQueenCastlePossible = true,
                isWhiteQueenCastlePossible = true,
                isWhiteKingCastlePossible = true,
                sideToMove = BLACK)
            // When
            assertThrows<IllegalStateException> {
                gameStateAggregate.applyMoveRequest(MoveRequest(E8,C8), gameState)
            }
        }

        @Test
        fun `should throw an error when attempting to castle while being checked`() {
            // Given
            val gameState = givenAChessGame(
                fenPiecePlacementOnly = "r3k2r/8/8/4R3/8/8/8/4K2R",
                isBlackKingCastlePossible = true,
                isBlackQueenCastlePossible = true,
                isWhiteQueenCastlePossible = true,
                isWhiteKingCastlePossible = true,
                sideToMove = BLACK)
            // When
            assertThrows<IllegalStateException> {
                gameStateAggregate.applyMoveRequest(MoveRequest(E8,C8), gameState)
            }
        }


    }
}

