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

class PositionPropertyBaseTest {


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

    private fun assertGameStateInvariantsAfterEachMove(position: Position) {
        assertThatSumOfCaptureAndRemainingPieceIs32(position)
        assertThatLastMovingPieceIsOnItsDestinationSquare(position)
        assertThatLastMovingHasLeftABlankSquareAfterMoving(position)
        assertVariousThingsAboutCastling(position)
        assertPawnShouldNotBeUnpromotedOnLastRank(position)
        assertOneBishopPerSquareColor(position)
    }

    private fun assertOneBishopPerSquareColor(position: Position) {
        if (position.history.moves.none { it.promotedTo == WHITE_BISHOP }) {
            expectThat(position.chessboard.count { it.value == WHITE_BISHOP && it.key.isLightSquare() }) isLessThanOrEqualTo 1
            expectThat(position.chessboard.count { it.value == WHITE_BISHOP && !it.key.isLightSquare() }) isLessThanOrEqualTo 1
        }
        if (position.history.moves.none { it.promotedTo == BLACK_BISHOP }) {
            expectThat(position.chessboard.count { it.value == BLACK_BISHOP && it.key.isLightSquare() }) isLessThanOrEqualTo 1
            expectThat(position.chessboard.count { it.value == BLACK_BISHOP && !it.key.isLightSquare() }) isLessThanOrEqualTo 1
        }
    }

    private fun assertPawnShouldNotBeUnpromotedOnLastRank(position: Position) {
        expectThat(
            position.chessboard.count { it.value == BLACK_PAWN && it.key.rank == Rank.RANK_1 }
        ).describedAs("We should not have black pawn on rank 1, it should have been promoted.")
            .isEqualTo(0)
        expectThat(
            position.chessboard.count { it.value == WHITE_PAWN && it.key.rank == Rank.RANK_8 }
        ).describedAs("We should not have white pawn on rank 8, it should have been promoted.")
            .isEqualTo(0)
    }


    private fun assertThatLastMovingPieceIsOnItsDestinationSquare(position: Position) {
        val lastMove = position.history.moves.last()
        if (lastMove.promotedTo == null) {
            expectThat(
                lastMove.let {
                    position.chessboard.getPieceAt(it.destination) == it.piece
                }
            ).describedAs(
                "At turn ${position.history.fullMoveCounter}, last move $lastMove should lead to a filled " +
                        "square at ${lastMove.destination} with a ${lastMove.piece} but was " +
                        position.chessboard.getPieceAt(lastMove.destination)
            )
                .isTrue()
        } else {
            expectThat(
                lastMove.let {
                    position.chessboard.getPieceAt(it.destination) == it.promotedTo
                }
            ).describedAs(
                "At turn ${position.history.fullMoveCounter}, last move $lastMove should lead to a filled " +
                        "square at ${lastMove.destination} with a ${lastMove.promotedTo} because of promotion but was " +
                        position.chessboard.getPieceAt(lastMove.destination)
            )
                .isTrue()
        }
    }

    private fun assertVariousThingsAboutCastling(position: Position) {

        position.castles.castles
            .filter { it.isCastleStillPossible }
            .forEach { castle ->
                expectThat(position.chessboard.getPieceAt(castle.rookStartSquare)).isNotNull()
                expectThat(position.chessboard.getPieceAt(castle.rookStartSquare)?.color) isEqualTo castle.color
                expectThat(position.chessboard.getPieceAt(castle.rookStartSquare)?.type) isEqualTo PieceType.ROOK

                expectThat(position.chessboard.getPieceAt(castle.kingStartSquare)).isNotNull()
                expectThat(position.chessboard.getPieceAt(castle.kingStartSquare)?.color) isEqualTo castle.color
                expectThat(position.chessboard.getPieceAt(castle.kingStartSquare)?.type) isEqualTo PieceType.KING
            }

        val historyMoves = position.history.moves
        val lastMove = position.history.moves.last()
        val movesWithoutLastMove = historyMoves.subList(0, historyMoves.lastIndex)
        if (lastMove.isKingCastle && lastMove.piece.color == Color.WHITE) {
            expectThat(movesWithoutLastMove.all { it.piece != WHITE_KING && it.origin != H1 }).isTrue()
            expectThat(position.chessboard.getPieceAt(H1)).isNull()
        }
        if (lastMove.isQueenCastle && lastMove.piece.color == Color.WHITE) {
            expectThat(movesWithoutLastMove.all { it.piece != WHITE_KING && it.origin != A1 }).isTrue()
            expectThat(position.chessboard.getPieceAt(B1)).isNull()
            expectThat(position.chessboard.getPieceAt(A1)).isNull()
        }

        if (lastMove.isKingCastle && lastMove.piece.color == Color.BLACK) {
            expectThat(movesWithoutLastMove.all { it.piece != BLACK_KING && it.origin != H8 }).isTrue()
            expectThat(position.chessboard.getPieceAt(H8)).isNull()
        }
        if (lastMove.isQueenCastle && lastMove.piece.color == Color.BLACK) {
            expectThat(movesWithoutLastMove.all { it.piece != BLACK_KING && it.origin != A8 }).isTrue()
            expectThat(position.chessboard.getPieceAt(B8)).isNull()
            expectThat(position.chessboard.getPieceAt(A8)).isNull()
        }
    }

    private fun assertThatLastMovingHasLeftABlankSquareAfterMoving(position: Position) {
        expectThat(
            position.history.moves.last().let {
                position.chessboard.getPieceAt(it.origin)
            }
        ).isNull()
    }

    private fun assertThatSumOfCaptureAndRemainingPieceIs32(position: Position) {

        val wholePieceCount = position.history.moves.count { it.capturedPiece != null } +
                position.chessboard.numberOfRemainingPieces()
        if (wholePieceCount != 32) {
            throw IllegalStateException()
        }
        expectThat(
            position.history.moves.count { it.capturedPiece != null } +
                    position.chessboard.numberOfRemainingPieces()
        ).isEqualTo(GameFactory.createStandardGame().chessboard.numberOfRemainingPieces())
    }
}