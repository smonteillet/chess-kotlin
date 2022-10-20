package fr.smo.chess.service

import fr.smo.chess.helper.RandomAIPlayer
import fr.smo.chess.model.*
import fr.smo.chess.model.Piece.Companion.blackKing
import fr.smo.chess.model.Piece.Companion.blackRook
import fr.smo.chess.model.Piece.Companion.whiteKing
import fr.smo.chess.model.Piece.Companion.whiteRook
import fr.smo.chess.model.Square.Companion.square
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
        fun randomSeeds() = (0..100).toList().map { Arguments.of(Random.nextInt()) }

        @JvmStatic
        fun specificSeeds() = listOf(
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

    private fun assertGameStateInvariantsAfterEachMove(gameState: GameState) {
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
            expectThat(gameState.chessboard.getPositionAt(square("a8"))).isNotNull()
            expectThat(gameState.chessboard.getPositionAt(square("a8"))?.piece) isEqualTo blackRook()
        }
        if (gameState.isBlackKingCastlePossible) {
            expectThat(gameState.chessboard.getPositionAt(square("h8"))).isNotNull()
            expectThat(gameState.chessboard.getPositionAt(square("h8"))?.piece) isEqualTo blackRook()
        }
        if (gameState.isWhiteQueenCastlePossible) {
            expectThat(gameState.chessboard.getPositionAt(square("a1"))).isNotNull()
            expectThat(gameState.chessboard.getPositionAt(square("a1"))?.piece) isEqualTo whiteRook()
        }
        if (gameState.isWhiteKingCastlePossible) {
            expectThat(gameState.chessboard.getPositionAt(square("h1"))).isNotNull()
            expectThat(gameState.chessboard.getPositionAt(square("h1"))?.piece) isEqualTo whiteRook()
        }

        if (gameState.isWhiteKingCastlePossible || gameState.isWhiteQueenCastlePossible) {
            expectThat(gameState.chessboard.getPositionAt(square("e1"))).isNotNull()
            expectThat(gameState.chessboard.getPositionAt(square("e1"))?.piece) isEqualTo whiteKing()
        }

        if (gameState.isBlackQueenCastlePossible || gameState.isBlackKingCastlePossible) {
            expectThat(gameState.chessboard.getPositionAt(square("e8"))).isNotNull()
            expectThat(gameState.chessboard.getPositionAt(square("e8"))?.piece) isEqualTo blackKing()
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
        ) isEqualTo GameState().chessboard.piecesOnBoard.size
    }
}