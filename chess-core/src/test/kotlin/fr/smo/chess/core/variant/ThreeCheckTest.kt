package fr.smo.chess.core.variant

import fr.smo.chess.core.MoveCommand
import fr.smo.chess.core.Square.*
import fr.smo.chess.core.Status
import fr.smo.chess.core.fixtures.GameStateFixtures.givenAChessGameWithHistory
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ThreeCheckTest {

    @Test
    fun `should win a three check game by performing three checks during game`() {
        // given
        val game = givenAChessGameWithHistory(
            variant = ThreeCheck,
            pgn = """
                1. d4 d5 
                2. c4 e6 
                3. Qa4+ Nc6 
                4. Qb3 Nb8 
                5. Qa4+ Nc6 
                6. Qb3 Nb8 
            """.trimIndent()
        )
        // when
        val updatedGame = game.applyMove(
            MoveCommand(B3, A4),
        ).orThrow()
        // then
        expectThat(updatedGame.status) isEqualTo Status.WHITE_WIN
    }

    @Test
    fun `should not a three check game by performing only two checks during game`() {
        // given
        val game = givenAChessGameWithHistory(
            variant = ThreeCheck,
            pgn = """
                1. d4 d5 
                2. c4 e6 
                3. Qa4+ Nc6 
                4. Qb3 Nb8 
            """
        )
        // when
        val updatedGame = game.applyMove(
            MoveCommand(B3, A4),
        ).orThrow()
        // then
        expectThat(updatedGame.status) isEqualTo Status.STARTED
    }

    @Test
    fun `should be able to win a three check game with a checkmate`() {
        val game = givenAChessGameWithHistory(
            variant = ThreeCheck,
            pgn = """
                1. f4 e5 
                2. g4 
            """
        )
        // when
        val updatedGame = game.applyMove(
            MoveCommand(D8, H4),
        ).orThrow()
        // then
        expectThat(updatedGame.status) isEqualTo Status.BLACK_WIN
    }
}