package fr.smo.chess.core

import fr.smo.chess.core.Piece.*
import fr.smo.chess.core.Square.*
import fr.smo.chess.core.fixtures.GameLoop
import fr.smo.chess.core.fixtures.RandomAIPlayer
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.*
import kotlin.random.Random

class GamePropertyBaseTest {


    companion object {
        @JvmStatic
        fun randomSeeds() = (0..10).toList().map { Arguments.of(Random.nextInt()) }

        @JvmStatic
        fun specificSeeds() = listOf(
            Arguments.of(887813262L)
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
        assertPawnShouldNotBeUnpromotedOnLastRank(game)
        assertOneBishopPerSquareColor(game)
    }

    private fun assertOneBishopPerSquareColor(game: Game) {
        if (game.history.moves.none { it.promotedTo == WHITE_BISHOP }) {
            expectThat(game.chessboard.count { it.value == WHITE_BISHOP && it.key.isLightSquare() }) isLessThanOrEqualTo 1
            expectThat(game.chessboard.count { it.value == WHITE_BISHOP && !it.key.isLightSquare() }) isLessThanOrEqualTo 1
        }
        if (game.history.moves.none { it.promotedTo == BLACK_BISHOP }) {
            expectThat(game.chessboard.count { it.value == BLACK_BISHOP && it.key.isLightSquare()}) isLessThanOrEqualTo 1
            expectThat(game.chessboard.count { it.value == BLACK_BISHOP && !it.key.isLightSquare()}) isLessThanOrEqualTo 1
        }
    }

    private fun assertPawnShouldNotBeUnpromotedOnLastRank(game: Game) {
        expectThat(
            game.chessboard.count { it.value == BLACK_PAWN && it.key.rank == Rank.RANK_1 }
        ).describedAs("We should not have black pawn on rank 1, it should have been promoted.")
            .isEqualTo(0)
        expectThat(
            game.chessboard.count { it.value == WHITE_PAWN && it.key.rank == Rank.RANK_8 }
        ).describedAs("We should not have white pawn on rank 8, it should have been promoted.")
            .isEqualTo(0)
    }


    private fun assertThatLastMovingPieceIsOnItsDestinationSquare(game: Game) {
        val lastMove = game.history.moves.last()
        if (lastMove.promotedTo == null) {
            expectThat(
                lastMove.let {
                    game.chessboard.getPieceAt(it.destination) == it.piece
                }
            ).describedAs(
                "At turn ${game.history.fullMoveCounter}, last move $lastMove should lead to a filled " +
                        "square at ${lastMove.destination} with a ${lastMove.piece} but was " +
                        game.chessboard.getPieceAt(lastMove.destination)
            )
                .isTrue()
        } else {
            expectThat(
                lastMove.let {
                    game.chessboard.getPieceAt(it.destination) == it.promotedTo
                }
            ).describedAs(
                "At turn ${game.history.fullMoveCounter}, last move $lastMove should lead to a filled " +
                        "square at ${lastMove.destination} with a ${lastMove.promotedTo} because of promotion but was " +
                        game.chessboard.getPieceAt(lastMove.destination)
            )
                .isTrue()
        }
    }

    private fun assertVariousThingsAboutCastling(game: Game) {
        if (game.castling.isBlackQueenCastlePossible) {
            expectThat(game.chessboard.getPieceAt(A8)).isNotNull()
            expectThat(game.chessboard.getPieceAt(A8)) isEqualTo BLACK_ROOK
        }
        if (game.castling.isBlackKingCastlePossible) {
            expectThat(game.chessboard.getPieceAt(H8)).isNotNull()
            expectThat(game.chessboard.getPieceAt(H8)) isEqualTo BLACK_ROOK
        }
        if (game.castling.isWhiteQueenCastlePossible) {
            expectThat(game.chessboard.getPieceAt(A1)).isNotNull()
            expectThat(game.chessboard.getPieceAt(A1)) isEqualTo WHITE_ROOK
        }
        if (game.castling.isWhiteKingCastlePossible) {
            expectThat(game.chessboard.getPieceAt(H1)).isNotNull()
            expectThat(game.chessboard.getPieceAt(H1)) isEqualTo WHITE_ROOK
        }

        if (game.castling.isWhiteKingCastlePossible || game.castling.isWhiteQueenCastlePossible) {
            expectThat(game.chessboard.getPieceAt(E1)).isNotNull()
            expectThat(game.chessboard.getPieceAt(E1)) isEqualTo WHITE_KING
        }

        if (game.castling.isBlackQueenCastlePossible || game.castling.isBlackKingCastlePossible) {
            expectThat(game.chessboard.getPieceAt(E8)).isNotNull()
            expectThat(game.chessboard.getPieceAt(E8)) isEqualTo BLACK_KING
        }

        val historyMoves = game.history.moves
        val lastMove = game.history.moves.last()
        val movesWithoutLastMove = historyMoves.subList(0, historyMoves.lastIndex)
        if (lastMove.isKingCastle && lastMove.piece.color == Color.WHITE) {
            expectThat(movesWithoutLastMove.all { it.piece != WHITE_KING  && it.origin != H1}).isTrue()
            expectThat(game.chessboard.getPieceAt(H1)).isNull()
        }
        if (lastMove.isQueenCastle && lastMove.piece.color == Color.WHITE) {
            expectThat(movesWithoutLastMove.all { it.piece != WHITE_KING  && it.origin != A1}).isTrue()
            expectThat(game.chessboard.getPieceAt(B1)).isNull()
            expectThat(game.chessboard.getPieceAt(A1)).isNull()
        }

        if (lastMove.isKingCastle && lastMove.piece.color == Color.BLACK) {
            expectThat(movesWithoutLastMove.all { it.piece != BLACK_KING  && it.origin != H8}).isTrue()
            expectThat(game.chessboard.getPieceAt(H8)).isNull()
        }
        if (lastMove.isQueenCastle && lastMove.piece.color == Color.BLACK) {
            expectThat(movesWithoutLastMove.all { it.piece != BLACK_KING  && it.origin != A8}).isTrue()
            expectThat(game.chessboard.getPieceAt(B8)).isNull()
            expectThat(game.chessboard.getPieceAt(A8)).isNull()
        }
    }

    private fun assertThatLastMovingHasLeftABlankSquareAfterMoving(game: Game) {
        expectThat(
            game.history.moves.last().let {
                game.chessboard.getPieceAt(it.origin)
            }
        ).isNull()
    }

    private fun assertThatSumOfCaptureAndRemainingPieceIs32(game: Game) {

        val wholePieceCount = game.history.moves.count { it.capturedPiece != null } +
                game.chessboard.numberOfRemainingPieces()
        if (wholePieceCount != 32) {
            throw IllegalStateException()
        }
        expectThat(
            game.history.moves.count { it.capturedPiece != null } +
                    game.chessboard.numberOfRemainingPieces()
        ).isEqualTo(GameFactory.createStandardGame().chessboard.numberOfRemainingPieces())
    }
}