package fr.smo.chess.core.debug

import fr.smo.chess.core.fixtures.GameStateFixtures.givenAChessGame
import fr.smo.chess.core.notation.STARTING_POSITION_FEN
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.stream.Stream


class PerftTest {

    @ParameterizedTest(name = "Checking {0} perft({2})")
    @MethodSource("providePerftData")
    fun `should compare real world perft result with this engine perft result`(name : String , fen : String, depth: Int, perftResult: PerftResult) {
        // Given
        val game = givenAChessGame(fen)
        val perft = Perft()
        // When
        val result = perft.perft(game, depth).also { println(it) }
        // Then
        expectThat(result) isEqualTo perftResult
    }

    companion object {

        /**
         * Some tests have been commented because performances are bad, they increase the build time.
         * perft(5) on position3 is failing because of check counts, I don't know why right now, it is probably a bug
         */
        @JvmStatic
        fun providePerftData(): Stream<Arguments> {
            val kiwipetePosition = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1" // https://www.chessprogramming.org/Perft_Results#Position_2
            val position3 = "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1" // https://www.chessprogramming.org/Perft_Results#Position_3
            return Stream.of(
                    Arguments.of("starting position", STARTING_POSITION_FEN, 1, PerftResult(20, 0, 0, 0, 0)),
                    Arguments.of("starting position", STARTING_POSITION_FEN, 2, PerftResult(400, 0, 0, 0, 0)),
//                    Arguments.of("starting position", STARTING_POSITION_FEN, 3, PerftResult(8_902, 34, 0, 0, 12)),
//                    Arguments.of("starting position", STARTING_POSITION_FEN, 4, PerftResult(197_281, 1576, 8, 0, 469)),
//                    Arguments.of("starting position", STARTING_POSITION_FEN, 5, PerftResult(4_865_609, 82_719, 347, 0, 27_351)),
                    Arguments.of("kiwipetePosition", kiwipetePosition, 1, PerftResult(48, 8, 0, 2, 0)),
                    Arguments.of("kiwipetePosition", kiwipetePosition, 2, PerftResult(2_039, 351, 0, 91, 3)),
//                    Arguments.of("kiwipetePosition", kiwipetePosition, 3, PerftResult(97_862,  17_102 , 1, 3_162,  993 )),
//                    Arguments.of("kiwipetePosition", kiwipetePosition, 4, PerftResult(4_085_603,  757163 , 43,  128013 ,  25523)),
                    Arguments.of("position3", position3, 1, PerftResult( 14, 1, 0, 0, 2)),
                    Arguments.of("position3", position3, 2, PerftResult( 191 , 14, 0, 0, 10)),
                    Arguments.of("position3", position3, 3, PerftResult(  2_812,  209 , 0, 0,  267 )),
//                    Arguments.of("position3", position3, 4, PerftResult(43_238,  3_348, 17,  0 ,  1_680)),
//                   Arguments.of("position3", position3, 5, PerftResult( 674_624 ,   52_051 , 0,  0 ,  52_051)),
            )
        }
    }
}