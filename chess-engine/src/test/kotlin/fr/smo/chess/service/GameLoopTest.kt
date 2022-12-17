package fr.smo.chess.service

import fr.smo.chess.ai.NegamaxAIPlayer
import fr.smo.chess.helper.RandomAIPlayer
import fr.smo.chess.model.*
import fr.smo.chess.model.Piece.*
import fr.smo.chess.model.Square.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isTrue
import kotlin.random.Random

internal class GameLoopTest {


    companion object {
        @JvmStatic
        fun randomSeeds() = (0..10).toList().map { Arguments.of(Random.nextInt()) }

        @JvmStatic
        fun specificSeeds()  = listOf(
            Arguments.of(1987108903L)
        )
    }

    @ParameterizedTest
    @MethodSource("randomSeeds")
    fun `should random chess game have a random outcome but without problems during the whole game with random seed`(
        seed: Long
    ) {
        val player1 = RandomAIPlayer(seed)
        val player2 = RandomAIPlayer(seed - 1)
        GameLoop(gameStateChangeNotifier = { assertGameStateInvariantsAfterEachMove(it) }).run(player1, player2)
    }

    @ParameterizedTest
    @MethodSource("specificSeeds")
    fun `should random chess game have a random outcome but without problems during the whole game with specific seed`() {
        `should random chess game have a random outcome but without problems during the whole game with random seed`(
            1987108903L
        )
    }

    @Test
    @Disabled
    fun `negamax with more depth AI player should win against negamax with lower depth`() {
       val player1 = NegamaxAIPlayer(3)
       val player2 = NegamaxAIPlayer(1)
        GameLoop(gameStateChangeNotifier = { assertGameStateInvariantsAfterEachMove(it) }).run(player1, player2)
    }

    private fun assertGameStateInvariantsAfterEachMove(gameState: GameState) {
        println(PGN().export(gameState))
        assertThatSumOfCaptureAndRemainingPieceIs32(gameState)
        assertThatLastMovingPieceIsOnItsDestinationSquare(gameState)
        assertThatLastMovingHasLeftABlankSquareAfterMoving(gameState)
        assertVariousThingsAboutCastling(gameState)
    }

    private fun assertThatLastMovingPieceIsOnItsDestinationSquare(gameState: GameState) {
        val lastMove = gameState.moveHistory.last()
        if (lastMove.promotedTo == null) {
            expectThat(
                lastMove.let {
                    gameState.chessboard.getPositionAt(it.destination)?.piece == it.piece
                }
            ).describedAs(
                "At turn ${gameState.fullMoveCounter}, last move $lastMove should lead to a filled " +
                        "square at ${lastMove.destination} with a ${lastMove.piece} but was " +
                        gameState.chessboard.getPositionAt(lastMove.destination)
            )
                .isTrue()
        } else {
            expectThat(
                lastMove.let {
                    gameState.chessboard.getPositionAt(it.destination)?.piece == it.promotedTo
                }
            ).describedAs(
                "At turn ${gameState.fullMoveCounter}, last move $lastMove should lead to a filled " +
                        "square at ${lastMove.destination} with a ${lastMove.promotedTo} because of promotion but was " +
                        gameState.chessboard.getPositionAt(lastMove.destination)
            )
                .isTrue()
        }
    }

    private fun assertVariousThingsAboutCastling(gameState: GameState) {
        if (gameState.isBlackQueenCastlePossible) {
            expectThat(gameState.chessboard.getPositionAt(A8)).isNotNull()
            expectThat(gameState.chessboard.getPositionAt(A8)?.piece) isEqualTo BLACK_ROOK
        }
        if (gameState.isBlackKingCastlePossible) {
            expectThat(gameState.chessboard.getPositionAt(H8)).isNotNull()
            expectThat(gameState.chessboard.getPositionAt(H8)?.piece) isEqualTo BLACK_ROOK
        }
        if (gameState.isWhiteQueenCastlePossible) {
            expectThat(gameState.chessboard.getPositionAt(A1)).isNotNull()
            expectThat(gameState.chessboard.getPositionAt(A1)?.piece) isEqualTo WHITE_ROOK
        }
        if (gameState.isWhiteKingCastlePossible) {
            expectThat(gameState.chessboard.getPositionAt(H1)).isNotNull()
            expectThat(gameState.chessboard.getPositionAt(H1)?.piece) isEqualTo WHITE_ROOK
        }

        if (gameState.isWhiteKingCastlePossible || gameState.isWhiteQueenCastlePossible) {
            expectThat(gameState.chessboard.getPositionAt(E1)).isNotNull()
            expectThat(gameState.chessboard.getPositionAt(E1)?.piece) isEqualTo WHITE_KING
        }

        if (gameState.isBlackQueenCastlePossible || gameState.isBlackKingCastlePossible) {
            expectThat(gameState.chessboard.getPositionAt(E8)).isNotNull()
            expectThat(gameState.chessboard.getPositionAt(E8)?.piece) isEqualTo BLACK_KING
        }
    }

    private fun assertThatLastMovingHasLeftABlankSquareAfterMoving(gameState: GameState) {
        expectThat(
            gameState.moveHistory.last().let {
                gameState.chessboard.getPositionAt(it.from)
            }
        ).isNull()
    }

    private fun assertThatSumOfCaptureAndRemainingPieceIs32(gameState: GameState) {
        expectThat(
            gameState.moveHistory.count { it.capturedPiece != null } +
                    gameState.chessboard.piecesOnBoard.size
        ) isEqualTo GameState.StartingPositions.NEW_STANDARD_CHESS_GAME.chessboard.piecesOnBoard.size
    }
}