package fr.smo.chess.core.variant

import fr.smo.chess.core.MoveCommand
import fr.smo.chess.core.Square
import fr.smo.chess.core.Status
import fr.smo.chess.core.fixtures.GameStateFixtures.givenAChess960Game
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class Chess960Test {

    @Test
    // Probably bad but real game I played against stockfish level 1 : https://lichess.org/0Kimw9i4/black#1
    fun `should win a chess 960 game by checkmate`() {
        // given
        val game = givenAChess960Game(
            startingPosition = "brkqnbnr",
            pgn = """
                1. b3 d5 
                2. Ngf3 b6 
                3. e3 Bb7 
                4. Bb5 Qd6 
                5. Ng5 O-O-O 
                6. b4 f6 
                7. Qh5 fxg5 
                8. Bd3 g6 
                9. Qh3+ Qd7 
                10. Bxg6 Qxh3 
                11. Bf5+ Qxf5 
                12. Bd4 e5 
                13. Bc3 d4 
                14. e4 dxc3 
                15. dxc3 Ngf6 
                16. Nd3 Bh6 
                17. f4 gxf4 
                18. Nc5 Bxe4 
                19. O-O-O f3+ 
                20. Rd2 Rxd2 
                21. gxf3 Bxc2 
                22. Nb3 Bxb3 
                23. f4 Bxf4 
                24. b5 Rd5+ 
                25. Kb2 Qc2+ 
                26. Ka1 
            """
        )
        // when
        val updatedGame = game.applyMove(
            MoveCommand(Square.C2, Square.A2)
        ).orThrow()
        // then
        expectThat(updatedGame.status) isEqualTo Status.BLACK_WIN
        expectThat(updatedGame.isCheckMate()) isEqualTo true
    }
}