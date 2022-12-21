package fr.smo.chess.core

import fr.smo.chess.core.Piece.*
import fr.smo.chess.core.Square.*
import fr.smo.chess.core.fixtures.GameLoop
import fr.smo.chess.core.fixtures.RandomAIPlayer
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isTrue
import kotlin.random.Random

class GamePropertyBaseTest {


    companion object {
        @JvmStatic
        fun randomSeeds() = (0..10).toList().map { Arguments.of(Random.nextInt()) }

        @JvmStatic
        fun specificSeeds()  = listOf(
            Arguments.of(1980302746L)
        )
    }

    @ParameterizedTest
    @MethodSource("randomSeeds")
    fun `should random chess game have a random outcome but without problems during the whole game with random seed`(
        seed: Long
    ) {
        val player1 = RandomAIPlayer(seed)
        val player2 = RandomAIPlayer(seed - 1) // seed - 1 because we don't want mirror match
        GameLoop(afterMoveCallback = { assertGameStateInvariantsAfterEachMove(it) }).run(player1, player2)
    }

    @ParameterizedTest
    @MethodSource("specificSeeds")
    fun `should random chess game have a random outcome but without problems during the whole game with specific seed`(
        seed: Long
    ) {
        `should random chess game have a random outcome but without problems during the whole game with random seed`(
            seed
        )
    }

    private fun assertGameStateInvariantsAfterEachMove(game: Game) {
        assertThatSumOfCaptureAndRemainingPieceIs32(game)
        assertThatLastMovingPieceIsOnItsDestinationSquare(game)
        assertThatLastMovingHasLeftABlankSquareAfterMoving(game)
        assertVariousThingsAboutCastling(game)
    }

    private fun assertThatLastMovingPieceIsOnItsDestinationSquare(game: Game) {
        val lastMove = game.moveHistory.last()
        if (lastMove.promotedTo == null) {
            expectThat(
                lastMove.let {
                    game.chessboard.getPositionAt(it.destination)?.piece == it.piece
                }
            ).describedAs(
                "At turn ${game.fullMoveCounter}, last move $lastMove should lead to a filled " +
                        "square at ${lastMove.destination} with a ${lastMove.piece} but was " +
                        game.chessboard.getPositionAt(lastMove.destination)
            )
                .isTrue()
        } else {
            expectThat(
                lastMove.let {
                    game.chessboard.getPositionAt(it.destination)?.piece == it.promotedTo
                }
            ).describedAs(
                "At turn ${game.fullMoveCounter}, last move $lastMove should lead to a filled " +
                        "square at ${lastMove.destination} with a ${lastMove.promotedTo} because of promotion but was " +
                        game.chessboard.getPositionAt(lastMove.destination)
            )
                .isTrue()
        }
    }

    private fun assertVariousThingsAboutCastling(game: Game) {
        if (game.castling.isBlackQueenCastlePossible) {
            expectThat(game.chessboard.getPositionAt(A8)).isNotNull()
            expectThat(game.chessboard.getPositionAt(A8)?.piece) isEqualTo BLACK_ROOK
        }
        if (game.castling.isBlackKingCastlePossible) {
            expectThat(game.chessboard.getPositionAt(H8)).isNotNull()
            expectThat(game.chessboard.getPositionAt(H8)?.piece) isEqualTo BLACK_ROOK
        }
        if (game.castling.isWhiteQueenCastlePossible) {
            expectThat(game.chessboard.getPositionAt(A1)).isNotNull()
            expectThat(game.chessboard.getPositionAt(A1)?.piece) isEqualTo WHITE_ROOK
        }
        if (game.castling.isWhiteKingCastlePossible) {
            expectThat(game.chessboard.getPositionAt(H1)).isNotNull()
            expectThat(game.chessboard.getPositionAt(H1)?.piece) isEqualTo WHITE_ROOK
        }

        if (game.castling.isWhiteKingCastlePossible || game.castling.isWhiteQueenCastlePossible) {
            expectThat(game.chessboard.getPositionAt(E1)).isNotNull()
            expectThat(game.chessboard.getPositionAt(E1)?.piece) isEqualTo WHITE_KING
        }

        if (game.castling.isBlackQueenCastlePossible || game.castling.isBlackKingCastlePossible) {
            expectThat(game.chessboard.getPositionAt(E8)).isNotNull()
            expectThat(game.chessboard.getPositionAt(E8)?.piece) isEqualTo BLACK_KING
        }
    }

    private fun assertThatLastMovingHasLeftABlankSquareAfterMoving(game: Game) {
        expectThat(
            game.moveHistory.last().let {
                game.chessboard.getPositionAt(it.from)
            }
        ).isNull()
    }

    private fun assertThatSumOfCaptureAndRemainingPieceIs32(game: Game) {
        expectThat(
            game.moveHistory.count { it.capturedPiece != null } +
                    game.chessboard.piecesOnBoard.size
        ) isEqualTo GameFactory.createStandardGame().chessboard.piecesOnBoard.size
    }
}