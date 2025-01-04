package fr.smo.chess.core.notation

import fr.smo.chess.core.*
import fr.smo.chess.core.Square.*
import org.junit.jupiter.api.*
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull
import strikt.assertions.isTrue
import java.io.File


class PGNTest {


    @Nested
    inner class Import {

        @Test
        fun `should import a pgn and ignore comment for a comment only pgn as input`() {
            val game = PGN.importPGN(
                """
                [COMMENT 1]
                [COMMENT 2]
            """
            )
            expectThat(game) isEqualTo GameFactory.createStandardGame()

        }

        @Test
        fun `should import a pgn with one turn of move`() {
            val game = PGN.importPGN("1. e4 e5")
            expectThat(game) isEqualTo GameFactory.createStandardGame().applyMoves(
                MoveCommand(E2, E4),
                MoveCommand(E7, E5),
            ).orThrow()
        }


        @Test
        fun `should import a pgn with move comment`() {
            val game = PGN.importPGN("1. e4 e5 {this is a comment}")
            expectThat(game) isEqualTo GameFactory.createStandardGame().applyMoves(
                MoveCommand(E2, E4),
                MoveCommand(E7, E5),
            ).orThrow()
        }

        @Test
        fun `should import a pgn with two turns of move`() {
            val game = PGN.importPGN("1. e4 e5 2. a3 a6")
            expectThat(game) isEqualTo GameFactory.createStandardGame().applyMoves(
                MoveCommand(E2, E4),
                MoveCommand(E7, E5),
                MoveCommand(A2, A3),
                MoveCommand(A7, A6),
            ).orThrow()

        }

        @Test
        fun `should import a pgn with knight moves`() {
            val game = PGN.importPGN("1. Nc3 Nf6")
            expectThat(game) isEqualTo GameFactory.createStandardGame().applyMoves(
                MoveCommand(B1, C3),
                MoveCommand(G8, F6),
            ).orThrow()
        }

        @Test
        fun `should import a pgn with all piece types moves`() {
            val game = PGN.importPGN("1. d4 d5 2. Bf4 Nf6 3. Qd3 Qd6 4. Kd2 Bf5 5. Nc3 Kd7 6. Rb1 Rg8")
            expectThat(game) isEqualTo GameFactory.createStandardGame().applyMoves(
                MoveCommand(D2, D4),
                MoveCommand(D7, D5),
                MoveCommand(C1, F4),
                MoveCommand(G8, F6),
                MoveCommand(D1, D3),
                MoveCommand(D8, D6),
                MoveCommand(E1, D2),
                MoveCommand(C8, F5),
                MoveCommand(B1, C3),
                MoveCommand(E8, D7),
                MoveCommand(A1, B1),
                MoveCommand(H8, G8),
            ).orThrow()
        }

        @Test
        fun `should import a pgn with a pawn capturing another pawn`() {
            val importedGame = PGN.importPGN("1. e4 d5 2. exd5")
            val expectedGame = GameFactory.createStandardGame().applyMoves(
                MoveCommand(E2, E4),
                MoveCommand(D7, D5),
                MoveCommand(E4, D5)
            ).orThrow()
            expectThat(importedGame) isEqualTo expectedGame
            val lastMove = importedGame.history.moves.last()
            expectThat(lastMove.capturedPiece) isEqualTo Piece.BLACK_PAWN
            expectThat(lastMove.piece) isEqualTo Piece.WHITE_PAWN
        }

        @Test
        fun `should import a pgn with a knight capturing another pawn`() {
            val importedGame = PGN.importPGN("1. Nc3 d5 2. Nxd5")
            val expectedGame = GameFactory.createStandardGame().applyMoves(
                MoveCommand(B1, C3),
                MoveCommand(D7, D5),
                MoveCommand(C3, D5)
            ).orThrow()
            expectThat(importedGame) isEqualTo expectedGame
            val lastMove = importedGame.history.moves.last()
            expectThat(lastMove.capturedPiece) isEqualTo Piece.BLACK_PAWN
            expectThat(lastMove.piece) isEqualTo Piece.WHITE_KNIGHT
        }

        @Test
        fun `should import a pgn with king castle for both white and black`() {
            val importedGame = PGN.importPGN("1. e4 e5 2. Bc4 Bc5 3. Nf3 Nf6 4. O-O O-O")
            val expectedGame = GameFactory.createStandardGame().applyMoves(
                MoveCommand(E2, E4),
                MoveCommand(E7, E5),
                MoveCommand(F1, C4),
                MoveCommand(F8, C5),
                MoveCommand(G1, F3),
                MoveCommand(G8, F6),
                MoveCommand(E1, G1),
                MoveCommand(E8, G8),
            ).orThrow()
            expectThat(importedGame) isEqualTo expectedGame
            expectThat(importedGame.history.moves.any { it.isKingCastle && it.piece.color == Color.BLACK }).isTrue()
            expectThat(importedGame.history.moves.any { it.isKingCastle && it.piece.color == Color.WHITE }).isTrue()
            expectThat(importedGame.castling.isWhiteKingCastlePossible).isFalse()
            expectThat(importedGame.castling.isWhiteQueenCastlePossible).isFalse()
            expectThat(importedGame.castling.isBlackKingCastlePossible).isFalse()
            expectThat(importedGame.castling.isBlackQueenCastlePossible).isFalse()
        }

        @Test
        fun `should import a pgn with queen castle for both white and black`() {
            val importedGame = PGN.importPGN("1. d4 d5 2. Bf4 Bf5 3. Nc3 Nc6 4. Qd3 Qd6 5. O-O-O O-O-O")
            val expectedGame = GameFactory.createStandardGame().applyMoves(
                MoveCommand(D2, D4),
                MoveCommand(D7, D5),
                MoveCommand(C1, F4),
                MoveCommand(C8, F5),
                MoveCommand(B1, C3),
                MoveCommand(B8, C6),
                MoveCommand(D1, D3),
                MoveCommand(D8, D6),
                MoveCommand(E1, C1),
                MoveCommand(E8, C8),
            ).orThrow()
            expectThat(importedGame) isEqualTo expectedGame
            expectThat(importedGame.history.moves.any { it.isQueenCastle && it.piece.color == Color.BLACK }).isTrue()
            expectThat(importedGame.history.moves.any { it.isQueenCastle && it.piece.color == Color.WHITE }).isTrue()
            expectThat(importedGame.castling.isWhiteKingCastlePossible).isFalse()
            expectThat(importedGame.castling.isWhiteQueenCastlePossible).isFalse()
            expectThat(importedGame.castling.isBlackKingCastlePossible).isFalse()
            expectThat(importedGame.castling.isBlackQueenCastlePossible).isFalse()
        }

        @Nested
        inner class CaptureWhenTwoPieceOfSameTypeCanCapture {

            @Test
            fun `should import a pgn with two knights of same color that can capture piece and Knight 1 captures`() {
                val importedGame = PGN.importPGN("1. Nc3 d5 2. Nh3 a6 3. Nf4 h6 4. Ncxd5 ")
                expectThat(importedGame.chessboard.getPieceAt(C3)).isNull()
                expectThat(importedGame.chessboard.getPieceAt(D5)!!) isEqualTo Piece.WHITE_KNIGHT
                expectThat(importedGame.chessboard.getPieceAt(F4)!!) isEqualTo Piece.WHITE_KNIGHT
            }

            @Test
            fun `should import a pgn with two knights of same color that can capture piece and Knight 2 captures`() {
                val importedGame = PGN.importPGN("1. Nc3 d5 2. Nh3 a6 3. Nf4 h6 4. Nfxd5 ")
                expectThat(importedGame.chessboard.getPieceAt(F4)).isNull()
                expectThat(importedGame.chessboard.getPieceAt(D5)!!) isEqualTo Piece.WHITE_KNIGHT
                expectThat(importedGame.chessboard.getPieceAt(C3)!!) isEqualTo Piece.WHITE_KNIGHT
            }

            @Test
            fun `should import a pgn with two rooks where either one or the other can move to the same given square`() {
                // Given
                val importedGame = PGN.importPGN("1. a4 a6 2. h4 h6 3. Rh3 b6 4. Rha3 c6 5. R3a2 ")
                expectThat(importedGame.chessboard.getPieceAt(A1)!!) isEqualTo Piece.WHITE_ROOK
                expectThat(importedGame.chessboard.getPieceAt(A2)!!) isEqualTo Piece.WHITE_ROOK
            }

            @Test
            fun `should import a pgn with two knights where either one or the other can move to the same given square and Knight 1 moves`() {
                // Given
                val importedGame = PGN.importPGN("1. Nc3 a6 2. Nf3 a5 3. Nd4 a4 4. Nb3 a3 5. Nc5 b6 6. N5e4 ")
                expectThat(importedGame.chessboard.getPieceAt(E4)!!) isEqualTo Piece.WHITE_KNIGHT
                expectThat(importedGame.chessboard.getPieceAt(C3)!!) isEqualTo Piece.WHITE_KNIGHT
                expectThat(importedGame.chessboard.getPieceAt(C5)).isNull()
            }

            @Test
            fun `should import a pgn with two knights where either one or the other can move to the same given square and Knight 2 moves`() {
                // Given
                val importedGame = PGN.importPGN("1. Nc3 a6 2. Nf3 a5 3. Nd4 a4 4. Nb3 a3 5. Nc5 b6 6. N3e4 ")
                expectThat(importedGame.chessboard.getPieceAt(E4)!!) isEqualTo Piece.WHITE_KNIGHT
                expectThat(importedGame.chessboard.getPieceAt(C5)!!) isEqualTo Piece.WHITE_KNIGHT
                expectThat(importedGame.chessboard.getPieceAt(C3)).isNull()
            }
        }

        @Test
        fun `should import a pgn with a promotion`() {
            val importedGame =
                PGN.importPGN("1. e4 e5 2. h3 g5 3. g4 h5 4. gxh5 Rh6 5. Bc4 Re6 6. h6 Rd6 7. h7 Re6 8. h8=Qe7")
            expectThat(importedGame.history.moves.last().promotedTo) isEqualTo Piece.WHITE_QUEEN
            expectThat(importedGame.chessboard.count { it.value == Piece.WHITE_PAWN }) isEqualTo 7
            expectThat(importedGame.chessboard.count { it.value == Piece.WHITE_QUEEN }) isEqualTo 2
        }

        @Test
        fun `should import pgn with two white rooks that can go to the same square but one is pinned at move 20`() {
            val importedGame = PGN.importPGN(
                """
                        1.e4 e5 2.Nf3 Nc6 3.Bc4 Bc5 4.O-O Nf6 5.b4 Bxb4 6.c3 Bd6 7.d4 Qe7 8.Bg5 O-O
                        9.Re1 a6 10.Qc2 h6 11.Bxf6 Qxf6 12.Nbd2 g5 13.dxe5 Bxe5 14.Rac1 g4 15.Nxe5 Nxe5
                        16.Bb3 h5 17.Re3 h4 18.Rf1 Kg7 19.f4 Qb6 20.Re1
                    """
            )
            expectThat(importedGame.chessboard.getPieceAt(E1)!!) isEqualTo Piece.WHITE_ROOK
            expectThat(importedGame.chessboard.getPieceAt(E3)!!) isEqualTo Piece.WHITE_ROOK
        }

        @Test
        fun `should import pgn with a castle move that checks`() {
            val importedGame = PGN.importPGN(
                """
                    1.e4 c5 2.Nf3 d6 3.d4 cxd4 4.Nxd4 Nf6 5.Nc3 a6 6.Bc4 Nc6 7.Nxc6 bxc6 8.e5 d5
                    9.exf6 dxc4 10.Qxd8+ Kxd8 11.fxe7+ Bxe7 12.Be3 Be6 13.O-O-O+ Kc8 14.Na4 Rb8
                    15.Bc5 Bg5+ 16.Kb1 Rb5 17.Bd4 Be7 18.Rhe1 Rd8 19.g3 g5 20.Nb6+ Kb7 21.Nxc4 Rbd5
                    22.c3 c5 23.Ne3 cxd4 24.Nxd5 Rxd5 25.Rxd4 Rf5 26.Rxe6 fxe6 27.Rd7+ Kc6 28.Rxe7 Kd5
                    29.Rd7+ Ke4 30.Rd2 Kf3 31.b4 e5 32.c4 e4 33.c5 Rf6 34.Rc2 Rf8 35.c6 h5 36.c7 Rc8
                    37.a4 h4 38.gxh4 gxh4 39.b5 h3 40.b6 Kg2 41.b7 Rxc7 42.Rxc7 Kxh2 43.b8=Q Kg2
                    44.Rg7+ Kh1 45.Qg3 h2 46.Qg2+  1-0  
                """.trimIndent()
            )
        }

        @Test
        fun `should import pgn with an unknown result`() {
            val importedGame = PGN.importPGN(
                """
                    1.c4 Nf6 2.g3 g6 3.Bg2 Bg7 4.Nc3 c5 5.e4 Nc6 6.Nge2 O-O 7.O-O Ne8 8.h3 Nc7
                    9.d3 Ne6 10.g4 Ned4 11.Ng3 e6 12.Be3 a6 13.Qd2 b5 14.f4 bxc4 15.dxc4 Rb8
                    16.b3 Bb7 17.Rac1 Ne7 18.g5 f5 19.gxf6 Bxf6 20.e5 Bh4 21.Nge4 Nef5 22.Bf2 Be7
                    23.Ne2 Bxe4 24.Bxe4 d5 25.cxd5 exd5 26.Bg2 Nxe2+ 27.Qxe2 d4 28.Qxa6 Kg7 29.Qd3 Rc8
                    30.Be4 Bh4 31.Bxf5 Rxf5 32.Qe4 Bxf2+ 33.Rxf2 Qh4 34.Qb7+ Kh6 35.Rcf1 Rd8
                    36.e6 d3 37.e7 Re8 38.Qd7 Qg3+ 39.Rg2 Qe3+ 40.Kh2 Rxe7 41.Qd8 Rxf4 42.Rg3 Rf2+
                    43.Rg2 Qf4+  *
                """.trimIndent()
            )
            expectThat(importedGame.status) isEqualTo Status.UNKNOWN_RESULT
            expectThat(importedGame.status.gameIsOver).isTrue()
        }

        @Nested
        inner class ImportFamousPosition {


            @TestFactory
            fun testPgnImportOfMorphyGames(): Collection<DynamicTest> {
                val morphyPgn = File("src/test/resources/morphy.pgn").readText(Charsets.UTF_8)
                return morphyPgn.trimIndent().split("\\[Event.*]".toRegex()).mapIndexed { index, pgnGame ->
                    DynamicTest.dynamicTest("Imported Morphy game $index") {
                        println(pgnGame)
                        PGN.importPGN(pgnGame)
                    }
                }
            }

            @TestFactory
            @Disabled("too long for now (about 20 second to execute). I keep it since it could be nice to execute only when I need it")
            fun testPgnImportOfPolgarGames(): Collection<DynamicTest> {
                val morphyPgn = File("src/test/resources/polgar.pgn").readText(Charsets.UTF_8)
                return morphyPgn.trimIndent().split("\\[Event.*]".toRegex()).map { pgnGame ->
                    DynamicTest.dynamicTest("Imported Polgar game") {
                        println(pgnGame)
                        PGN.importPGN(pgnGame)
                    }
                }
            }

            @TestFactory
            @Disabled("too long for now (about 45 second to execute). I keep it since it could be nice to execute only when I need it")
            fun testPgnImportOfCarlsenGames(): Collection<DynamicTest> {
                val morphyPgn = File("src/test/resources/carlsen.pgn").readText(Charsets.UTF_8)
                return morphyPgn.trimIndent().split("\\[Event.*]".toRegex()).map { pgnGame ->
                    DynamicTest.dynamicTest("Imported Carlsen game") {
                        println(pgnGame)
                        PGN.importPGN(pgnGame)
                    }
                }
            }

        }

    }
}