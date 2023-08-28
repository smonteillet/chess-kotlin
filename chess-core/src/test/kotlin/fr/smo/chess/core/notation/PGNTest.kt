package fr.smo.chess.core.notation

import fr.smo.chess.core.*
import fr.smo.chess.core.Square.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull
import strikt.assertions.isTrue

class PGNTest {


    @Nested
    inner class Import {

        @Test
        fun `should import a pgn and ignore comment for a comment only pgn as input`() {
            val game = PGN.import(
                """
                [COMMENT 1]
                [COMMENT 2]
            """
            )
            expectThat(game) isEqualTo GameFactory.createStandardGame()

        }

        @Test
        fun `should import a pgn with one turn of move`() {
            val game = PGN.import("1. e4 e5")
            expectThat(game) isEqualTo GameFactory.createStandardGame().applyMoves(
                MoveRequest(E2, E4),
                MoveRequest(E7, E5),
            )
        }


        @Test
        fun `should import a pgn with move comment`() {
            val game = PGN.import("1. e4 e5 {this is a comment}")
            expectThat(game) isEqualTo GameFactory.createStandardGame().applyMoves(
                MoveRequest(E2, E4),
                MoveRequest(E7, E5),
            )
        }

        @Test
        fun `should import a pgn with two turns of move`() {
            val game = PGN.import("1. e4 e5 2. a3 a6")
            expectThat(game) isEqualTo GameFactory.createStandardGame().applyMoves(
                MoveRequest(E2, E4),
                MoveRequest(E7, E5),
                MoveRequest(A2, A3),
                MoveRequest(A7, A6),
            )

        }

        @Test
        fun `should import a pgn with knight moves`() {
            val game = PGN.import("1. Nc3 Nf6")
            expectThat(game) isEqualTo GameFactory.createStandardGame().applyMoves(
                MoveRequest(B1, C3),
                MoveRequest(G8, F6),
            )
        }

        @Test
        fun `should import a pgn with all piece types moves`() {
            val game = PGN.import("1. d4 d5 2. Bf4 Nf6 3. Qd3 Qd6 4. Kd2 Bf5 5. Nc3 Kd7 6. Rb1 Rg8")
            expectThat(game) isEqualTo GameFactory.createStandardGame().applyMoves(
                MoveRequest(D2, D4),
                MoveRequest(D7, D5),
                MoveRequest(C1, F4),
                MoveRequest(G8, F6),
                MoveRequest(D1, D3),
                MoveRequest(D8, D6),
                MoveRequest(E1, D2),
                MoveRequest(C8, F5),
                MoveRequest(B1, C3),
                MoveRequest(E8, D7),
                MoveRequest(A1, B1),
                MoveRequest(H8, G8),
            )
        }

        @Test
        fun `should import a pgn with a pawn capturing another pawn`() {
            val importedGame = PGN.import("1. e4 d5 2. exd5")
            val expectedGame = GameFactory.createStandardGame().applyMoves(
                MoveRequest(E2, E4),
                MoveRequest(D7, D5),
                MoveRequest(E4, D5)
            )
            expectThat(importedGame) isEqualTo expectedGame
            val lastMove = importedGame.history.moves.last()
            expectThat(lastMove.capturedPiece) isEqualTo Piece.BLACK_PAWN
            expectThat(lastMove.piece) isEqualTo Piece.WHITE_PAWN
        }

        @Test
        fun `should import a pgn with a knight capturing another pawn`() {
            val importedGame = PGN.import("1. Nc3 d5 2. Nxd5")
            val expectedGame = GameFactory.createStandardGame().applyMoves(
                MoveRequest(B1, C3),
                MoveRequest(D7, D5),
                MoveRequest(C3, D5)
            )
            expectThat(importedGame) isEqualTo expectedGame
            val lastMove = importedGame.history.moves.last()
            expectThat(lastMove.capturedPiece) isEqualTo Piece.BLACK_PAWN
            expectThat(lastMove.piece) isEqualTo Piece.WHITE_KNIGHT
        }

        @Test
        fun `should import a pgn with king castle for both white and black`() {
            val importedGame = PGN.import("1. e4 e5 2. Bc4 Bc5 3. Nf3 Nf6 4. O-O O-O")
            val expectedGame = GameFactory.createStandardGame().applyMoves(
                MoveRequest(E2, E4),
                MoveRequest(E7, E5),
                MoveRequest(F1, C4),
                MoveRequest(F8, C5),
                MoveRequest(G1, F3),
                MoveRequest(G8, F6),
                MoveRequest(E1, G1),
                MoveRequest(E8, G8),

                )
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
            val importedGame = PGN.import("1. d4 d5 2. Bf4 Bf5 3. Nc3 Nc6 4. Qd3 Qd6 5. O-O-O O-O-O")
            val expectedGame = GameFactory.createStandardGame().applyMoves(
                MoveRequest(D2, D4),
                MoveRequest(D7, D5),
                MoveRequest(C1, F4),
                MoveRequest(C8, F5),
                MoveRequest(B1, C3),
                MoveRequest(B8, C6),
                MoveRequest(D1, D3),
                MoveRequest(D8, D6),
                MoveRequest(E1, C1),
                MoveRequest(E8, C8),

                )
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
                val importedGame = PGN.import("1. Nc3 d5 2. Nh3 a6 3. Nf4 h6 4. Ncxd5 ")
                expectThat(importedGame.chessboard.getPositionAt(C3)).isNull()
                expectThat(importedGame.chessboard.getPositionAt(D5)!!.piece) isEqualTo Piece.WHITE_KNIGHT
                expectThat(importedGame.chessboard.getPositionAt(F4)!!.piece) isEqualTo Piece.WHITE_KNIGHT
            }

            @Test
            fun `should import a pgn with two knights of same color that can capture piece and Knight 2 captures`() {
                val importedGame = PGN.import("1. Nc3 d5 2. Nh3 a6 3. Nf4 h6 4. Nfxd5 ")
                expectThat(importedGame.chessboard.getPositionAt(F4)).isNull()
                expectThat(importedGame.chessboard.getPositionAt(D5)!!.piece) isEqualTo Piece.WHITE_KNIGHT
                expectThat(importedGame.chessboard.getPositionAt(C3)!!.piece) isEqualTo Piece.WHITE_KNIGHT
            }

            @Test
            fun `should import a pgn with two rooks where either one or the other can move to the same given square`() {
                // Given
                val importedGame = PGN.import("1. a4 a6 2. h4 h6 3. Rh3 b6 4. Rha3 c6 5. R3a2 ")
                expectThat(importedGame.chessboard.getPositionAt(A1)!!.piece) isEqualTo Piece.WHITE_ROOK
                expectThat(importedGame.chessboard.getPositionAt(A2)!!.piece) isEqualTo Piece.WHITE_ROOK
            }

            @Test
            fun `should import a pgn with two knights where either one or the other can move to the same given square and Knight 1 moves`() {
                // Given
                val importedGame = PGN.import("1. Nc3 a6 2. Nf3 a5 3. Nd4 a4 4. Nb3 a3 5. Nc5 b6 6. N5e4 ")
                expectThat(importedGame.chessboard.getPositionAt(E4)!!.piece) isEqualTo Piece.WHITE_KNIGHT
                expectThat(importedGame.chessboard.getPositionAt(C3)!!.piece) isEqualTo Piece.WHITE_KNIGHT
                expectThat(importedGame.chessboard.getPositionAt(C5)).isNull()
            }

            @Test
            fun `should import a pgn with two knights where either one or the other can move to the same given square and Knight 2 moves`() {
                // Given
                val importedGame = PGN.import("1. Nc3 a6 2. Nf3 a5 3. Nd4 a4 4. Nb3 a3 5. Nc5 b6 6. N3e4 ")
                expectThat(importedGame.chessboard.getPositionAt(E4)!!.piece) isEqualTo Piece.WHITE_KNIGHT
                expectThat(importedGame.chessboard.getPositionAt(C5)!!.piece) isEqualTo Piece.WHITE_KNIGHT
                expectThat(importedGame.chessboard.getPositionAt(C3)).isNull()
            }
        }

        @Test
        fun `should import a pgn with a promotion`() {
            val importedGame = PGN.import("1. e4 e5 2. h3 g5 3. g4 h5 4. gxh5 Rh6 5. Bc4 Re6 6. h6 Rd6 7. h7 Re6 8. h8=Qe7")
            expectThat(importedGame.history.moves.last().promotedTo) isEqualTo Piece.WHITE_QUEEN
            expectThat(importedGame.chessboard.piecesOnBoard.count { it.piece == Piece.WHITE_PAWN }) isEqualTo 7
            expectThat(importedGame.chessboard.piecesOnBoard.count { it.piece == Piece.WHITE_QUEEN }) isEqualTo 2

        }

        @Test
        fun `should import famous game pgn Fisher Vs Spassky 1992 Belgrade`() {
            // Given
            val importedGame = PGN.import("""
                [Event "F/S Return Match"]
                [Site "Belgrade, Serbia JUG"]
                [Date "1992.11.04"]
                [Round "29"]
                [White "Fischer, Robert J."]
                [Black "Spassky, Boris V."]
                [Result "1/2-1/2"]
                
                1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 {This opening is called the Ruy Lopez.}
                4. Ba4 Nf6 5. O-O Be7 6. Re1 b5 7. Bb3 d6 8. c3 O-O 9. h3 Nb8 10. d4 Nbd7
                11. c4 c6 12. cxb5 axb5 13. Nc3 Bb7 14. Bg5 b4 15. Nb1 h6 16. Bh4 c5 17. dxe5
                Nxe4 18. Bxe7 Qxe7 19. exd6 Qf6 20. Nbd2 Nxd6 21. Nc4 Nxc4 22. Bxc4 Nb6
                23. Ne5 Rae8 24. Bxf7+ Rxf7 25. Nxf7 Rxe1+ 26. Qxe1 Kxf7 27. Qe3 Qg5 28. Qxg5
                hxg5 29. b3 Ke6 30. a3 Kd6 31. axb4 cxb4 32. Ra5 Nd5 33. f3 Bc8 34. Kf2 Bf5
                35. Ra7 g6 36. Ra6+ Kc5 37. Ke1 Nf4 38. g3 Nxh3 39. Kd2 Kb5 40. Rd6 Kc5 41. Ra6
                Nf2 42. g4 Bd3 43. Re6 1/2-1/2
            """)

            expectThat(importedGame.gameIsOver).isTrue()
            expectThat(importedGame.status) isEqualTo Game.Status.DRAW
            expectThat(importedGame.history.moves.size) isEqualTo 85


        }
    }

}