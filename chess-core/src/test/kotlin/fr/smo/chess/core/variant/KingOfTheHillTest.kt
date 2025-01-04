package fr.smo.chess.core.variant

import fr.smo.chess.core.MoveCommand
import fr.smo.chess.core.Square.*
import fr.smo.chess.core.Status
import fr.smo.chess.core.fixtures.GameStateFixtures.givenAChessGameWithHistory
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class KingOfTheHillTest {

    @Test
    fun `should be able to win a KOTH game by reaching a central square as the king`() {
        // given
        val game = givenAChessGameWithHistory(
            variant = KingOfTheHill,
            pgn = """
                1. e4 d5 
                2. f3 Kd7 
                3. d4 Kd6 
                4. c3 c5 
                5. Bd3 cxd4 
                6. cxd4 e5 
                7. Be3 dxe4 
                8. fxe4 Bf5 
                9. Nc3 Bxe4
                10. Bxe4 Qc8
                11. Nf3 exd4 
                12. Qxd4+ Ke6 
                13. Kd2 Nc6 
                14. Kd3 Nce7 
                15. Bd5+ Nxd5 
            """
        )
        // when
        val updatedGame = game.applyMove(
            MoveCommand(D3, E4),
        ).orThrow()
        // then
        expectThat(updatedGame.status) isEqualTo Status.WHITE_WIN
    }

    @Test
    fun `should be able to win a three check game with a checkmate`() {
        val game = givenAChessGameWithHistory(
            variant = KingOfTheHill,
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