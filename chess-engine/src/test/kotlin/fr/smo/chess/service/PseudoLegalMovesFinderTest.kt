package fr.smo.chess.service

import fr.smo.chess.fixtures.GameStateFixtures.Companion.givenAChessGame
import fr.smo.chess.model.Color.BLACK
import fr.smo.chess.model.Color.WHITE
import fr.smo.chess.model.Move
import fr.smo.chess.model.Piece.Companion.blackBishop
import fr.smo.chess.model.Piece.Companion.blackKing
import fr.smo.chess.model.Piece.Companion.blackPawn
import fr.smo.chess.model.Piece.Companion.whiteBishop
import fr.smo.chess.model.Piece.Companion.whiteKing
import fr.smo.chess.model.Piece.Companion.whiteKnight
import fr.smo.chess.model.Piece.Companion.whitePawn
import fr.smo.chess.model.Piece.Companion.whiteQueen
import fr.smo.chess.model.Piece.Companion.whiteRook
import fr.smo.chess.model.Piece.Type.*
import fr.smo.chess.model.Position
import fr.smo.chess.model.Square.Companion.square
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

internal class PseudoLegalMovesFinderTest {

    private val pseudoLegalMovesFinder = PseudoLegalMovesFinder()

    @Nested
    inner class Pawn {

        @Nested
        inner class White {
            @Test
            fun `should return a one square move and no two squares move when a piece is blocking the two squares move`() {
                // given
                val gameState =  givenAChessGame(
                    fenPiecePlacementOnly = "8/8/8/8/1P6/8/1P6/8",
                )
                // when
                val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                    Position(square("b2"), whitePawn()),
                    gameState
                )
                // then
                expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder("b3")
            }

            @Test
            fun `should return no moves when a piece with the same color is blocking one square away`() {
                // given
                val gameState =  givenAChessGame(
                    fenPiecePlacementOnly = "8/8/8/8/8/1P6/1P6/8",
                )
                // when
                val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                    Position(square("b2"), whitePawn()),
                    gameState
                )
                // then
                expectThat(legalMoves).isEmpty()
            }

            @Test
            fun `should return no moves when a piece with opposite color is blocking one square away`() {
                // given
                val gameState =  givenAChessGame(
                    fenPiecePlacementOnly = "8/8/8/8/8/1p6/1P6/8",
                )
                // when
                val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                    Position(square("b2"), whitePawn()),
                    gameState
                )
                // then
                expectThat(legalMoves).isEmpty()
            }

            @Test
            fun `should return a one square move and a two square moves when possible`() {
                // given
                val gameState =  givenAChessGame(
                    fenPiecePlacementOnly = "8/8/8/8/8/8/1P6/8",
                )
                // when
                val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                    Position(square("b2"), whitePawn()),
                    gameState
                )
                // then
                expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder("b3", "b4")
            }

            @Test
            fun `should not return a two squares move if pawn has previously moved`() {
                // given
                val gameState =  givenAChessGame(
                    fenPiecePlacementOnly = "8/8/8/8/8/1P6/8/8",
                )
                // when
                val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                    Position(square("b3"), whitePawn()),
                    gameState
                )
                // then
                expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder("b4")
            }

            @Test
            fun `should capture black piece at right diagonal move when possible`() {
                // given
                val gameState =  givenAChessGame(
                    fenPiecePlacementOnly = "8/8/8/8/2p5/1P6/8/8",
                )
                // when
                val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                    Position(square("b3"), whitePawn()),
                    gameState
                )
                // then
                expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder("b4", "c4")
                expectThat(legalMoves.contains(Move(
                    piece = whitePawn(),
                    from = square("b3"),
                    destination = square("c4"),
                    capturedPiece = blackPawn(),
                ))).isTrue()
            }


            @Test
            fun `should capture black piece at left diagonal move when possible`() {
                // given
                val gameState =  givenAChessGame(
                    fenPiecePlacementOnly = "8/8/8/8/p7/1P6/8/8",
                )
                // when
                val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                    Position(square("b3"), whitePawn()),
                    gameState
                )
                // then
                expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder("b4", "a4")
                expectThat(legalMoves.contains(Move(
                    piece = whitePawn(),
                    from = square("b3"),
                    destination = square("a4"),
                    capturedPiece = blackPawn(),
                ))).isTrue()
            }

            @Test
            fun `should capture black piece at left diagonal and right diagonal moves when possible`() {
                // given
                val gameState =  givenAChessGame(
                    fenPiecePlacementOnly = "8/8/8/8/p1p5/1P6/8/8",
                )
                // when
                val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                    Position(square("b3"), whitePawn()),
                    gameState
                )
                // then
                expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder("b4", "a4", "c4")
                expectThat(legalMoves.contains(Move(
                    piece = whitePawn(),
                    from = square("b3"),
                    destination = square("a4"),
                    capturedPiece = blackPawn(),
                ))).isTrue()
                expectThat(legalMoves.contains(Move(
                    piece = whitePawn(),
                    from = square("b3"),
                    destination = square("c4"),
                    capturedPiece = blackPawn(),
                ))).isTrue()
            }

            @Test
            fun `should promote when possible`() {
                // given
                val gameState =  givenAChessGame(
                    fenPiecePlacementOnly = "8/2P5/8/8/8/8/8/8",
                )
                // when
                val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                    Position(square("c7"), whitePawn()),
                    gameState
                )
                // Then
                expectThat(legalMoves.map { it.promotedTo!!.type }).containsExactlyInAnyOrder(
                    QUEEN,
                    ROOK,
                    BISHOP,
                    KNIGHT
                )
                expectThat(legalMoves.map { it.promotedTo!!.color }.all { it == WHITE }) isEqualTo true
                expectThat(legalMoves.map { it.destination }.all { it == square("c8") }) isEqualTo true
            }

            @Nested
            inner class EnPassant {

               @Test
                fun `should take en passant to the left when possible`() {
                   // Given
                   val gameState =  givenAChessGame(
                       fenPiecePlacementOnly = "8/8/8/2pP4/8/8/8/8",
                       enPassantTargetSquare = square("c6")
                   )
                   // when
                   val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                       Position(square("d5"), whitePawn()),
                       gameState
                   )
                    // Then
                    expectThat(legalMoves).containsExactlyInAnyOrder(
                        Move(whitePawn(), square("d5"), square("d6")),
                        Move(whitePawn(), square("d5"), square("c6"), capturedPiece = blackPawn())
                    )
                }

                @Test
                fun `should take en passant to the right when possible`() {
                    // Given
                    val gameState =  givenAChessGame(
                        fenPiecePlacementOnly = "8/8/8/3Pp3/8/8/8/8",
                        enPassantTargetSquare = square("e6")
                    )
                    // when
                    val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                        Position(square("d5"), whitePawn()),
                        gameState
                    )
                    // Then
                    expectThat(legalMoves).containsExactlyInAnyOrder(
                        Move(whitePawn(), square("d5"), square("d6")),
                        Move(whitePawn(), square("d5"), square("e6"), capturedPiece = blackPawn())
                    )
                }

                @Test
                fun `should not take en passant when opposite piece is not a pawn on row 6`() {
                    // Given
                    val gameState =  givenAChessGame(
                        fenPiecePlacementOnly = "8/8/8/2bP4/8/8/8/8"
                    )
                    // when
                    val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                        Position(square("d5"), whitePawn()),
                        gameState
                    )
                    // Then
                    expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(
                        square("d6")
                    )
                }
            }
        }

        @Nested
        inner class Black {

            @Test
            fun `should return a one square move and no two squares move when a piece is blocking the two squares move`() {
                // Given
                val gameState =  givenAChessGame(
                    fenPiecePlacementOnly = "8/1p6/8/1p6/8/8/8/8"
                )
                // when
                val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                    Position(square("b7"), blackPawn()),
                    gameState
                )
                // then
                expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder("b6")
            }

            @Test
            fun `should return no moves when a piece with the same color is blocking one square away`() {
                // Given
                val gameState =  givenAChessGame(
                    fenPiecePlacementOnly = "8/1p6/1p6/8/8/8/8/8"
                )
                // when
                val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                    Position(square("b7"), blackPawn()),
                    gameState
                )
                // then
                expectThat(legalMoves).isEmpty()
            }

            @Test
            fun `should return no moves when a piece with opposite color is blocking one square away`() {
                // Given
                val gameState =  givenAChessGame(
                    fenPiecePlacementOnly = "8/1p6/1P6/8/8/8/8/8"
                )
                // when
                val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                    Position(square("b7"), blackPawn()),
                    gameState
                )
                // then
                expectThat(legalMoves).isEmpty()
            }

            @Test
            fun `should return a one square move and a two square moves when possible`() {
                // Given
                val gameState =  givenAChessGame(
                    fenPiecePlacementOnly = "8/1p6/8/8/8/8/8/8"
                )
                // when
                val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                    Position(square("b7"), blackPawn()),
                    gameState
                )
                // then
                expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder("b6", "b5")
            }

            @Test
            fun `should not return a two squares move if pawn has previously moved`() {
                // Given
                val gameState =  givenAChessGame(
                    fenPiecePlacementOnly = "8/8/1p6/8/8/8/8/8"
                )
                // when
                val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                    Position(square("b6"), blackPawn()),
                    gameState
                )
                // then
                expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder("b5")
            }

            @Test
            fun `should capture white pawn at right diagonal  when possible`() {
                // Given
                val gameState =  givenAChessGame(
                    fenPiecePlacementOnly = "8/8/1p6/2P5/8/8/8/8"
                )
                // when
                val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                    Position(square("b6"), blackPawn()),
                    gameState
                )
                // then
                expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder("b5", "c5")
                expectThat(legalMoves.contains(Move(
                    piece = blackPawn(),
                    from = square("b6"),
                    destination = square("c5"),
                    capturedPiece = whitePawn(),
                )))
            }


            @Test
            fun `should capture white pawn at left diagonal  when possible`() {
                // Given
                val gameState =  givenAChessGame(
                    fenPiecePlacementOnly = "8/8/1p6/P7/8/8/8/8"
                )
                // when
                val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                    Position(square("b6"), blackPawn()),
                    gameState
                )
                // then
                expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder("b5", "a5")
                expectThat(legalMoves.contains(Move(
                    piece = blackPawn(),
                    from = square("b6"),
                    destination = square("a5"),
                    capturedPiece = whitePawn(),
                )))
            }

            @Test
            fun `should capture piece at left diagonal and right diagonal when possible`() {
                // Given
                val gameState =  givenAChessGame(
                    fenPiecePlacementOnly = "8/8/1p6/P1P5/8/8/8/8"
                )
                // when
                val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                    Position(square("b6"), blackPawn()),
                    gameState
                )
                // then
                expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder("b5", "a5", "c5")
                expectThat(legalMoves.contains(Move(
                    piece = blackPawn(),
                    from = square("b6"),
                    destination = square("a5"),
                    capturedPiece = whitePawn(),
                )))
                expectThat(legalMoves.contains(Move(
                    piece = blackPawn(),
                    from = square("b6"),
                    destination = square("c5"),
                    capturedPiece = whitePawn(),
                )))
            }

            @Test
            fun `should promote when possible`() {
                // Given
                val gameState =  givenAChessGame(
                    fenPiecePlacementOnly = "8/8/8/8/8/8/1p6/8"
                )
                // when
                val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                    Position(square("b2"), blackPawn()),
                    gameState
                )
                // Then
                expectThat(legalMoves.map { it.promotedTo!!.type }).containsExactlyInAnyOrder(
                    QUEEN,
                    ROOK,
                    BISHOP,
                    KNIGHT
                )
                expectThat(legalMoves.map { it.promotedTo!!.color }.all { it == BLACK }) isEqualTo true
                expectThat(legalMoves.map { it.destination }.all { it == square("b1") }) isEqualTo true
            }

            @Nested
            inner class EnPassant {

                @Test
                fun `should take en passant to the left when possible`() {
                    // Given
                    val gameState =  givenAChessGame(
                        fenPiecePlacementOnly = "8/8/8/8/3Pp3/8/8/8",
                        enPassantTargetSquare = square("d3"),
                    )
                    // when
                    val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                        Position(square("e4"), blackPawn()),
                        gameState
                    )
                    expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder("d3", "e3")
                    expectThat(legalMoves.contains(Move(
                        piece = blackPawn(),
                        from = square("e4"),
                        destination = square("d3"),
                        capturedPiece = whitePawn(),
                    ))).isTrue()
                }

                @Test
                fun `should take en passant to the right when possible`() {
                    // Given
                    val gameState =  givenAChessGame(
                        fenPiecePlacementOnly = "8/8/8/8/2pP4/8/8/8",
                        enPassantTargetSquare = square("d3"),
                    )
                    // when
                    val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                        Position(square("c4"), blackPawn()),
                        gameState
                    )
                    expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder("d3", "c3")
                    expectThat(legalMoves.contains(Move(
                        piece = blackPawn(),
                        from = square("c4"),
                        destination = square("d3"),
                        capturedPiece = whitePawn(),
                    ))).isTrue()
                }

                @Test
                fun `should not take en passant when opposite piece is not a pawn on row 6`() {
                    // Given
                    val gameState =  givenAChessGame(
                        fenPiecePlacementOnly = "8/8/8/8/2Bp4/8/8/8",
                        enPassantTargetSquare = null,
                    )
                    // when
                    val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                        Position(square("d4"), blackPawn()),
                        gameState
                    )
                    expectThat(legalMoves.map{ it.destination.toString() }).containsExactlyInAnyOrder("d3")
                }

                @Test
                fun `should not take en passant when opposite pawn on raw 6 has not made a two square move at first`() {
                    // Given
                    val gameState =  givenAChessGame(
                        fenPiecePlacementOnly = "8/8/8/8/2Pp4/8/8/8",
                        enPassantTargetSquare = null,
                    )
                    // when
                    val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                        Position(square("d4"), blackPawn()),
                        gameState
                    )
                    expectThat(legalMoves.map{ it.destination.toString() }).containsExactlyInAnyOrder("d3")
                }
            }
        }

    }

    @Nested
    inner class Knight {
        @Test
        fun `should return all 8 knight moves when possible`() {
            // given
            val gameState =  givenAChessGame(
                fenPiecePlacementOnly = "8/8/8/8/4N3/8/8/8",
            )
            // when
            val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                Position(square("e4"), whiteKnight()),
                gameState
            )
            // then
            expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder(
                "g5", "g3", "c5", "c3", "f6", "f2", "d6", "d2"
            )
        }

        @Test
        fun `should not return out of bound moves`() {
            // given
            val gameState =  givenAChessGame(
                fenPiecePlacementOnly = "8/8/8/8/8/8/8/N7",
            )
            // when
            val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                Position(square("a1"), whiteKnight()),
                gameState
            )
            // then
            expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder(
                "c2", "b3"
            )
        }

        @Test
        fun `should not move to square where it has a same color piece`() {
            // given
            val gameState =  givenAChessGame(
                fenPiecePlacementOnly = "8/8/8/6B1/4N3/8/8/8",
            )
            // when
            val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                Position(square("e4"), whiteKnight()),
                gameState
            )
            // Then
            expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder(
                "g3", "c5", "c3", "f6", "f2", "d6", "d2"
            )
        }

        @Test
        fun `should move to square where it can take a piece`() {
            // given
            val gameState =  givenAChessGame(
                fenPiecePlacementOnly = "8/8/8/6b1/4N3/8/8/8",
            )
            // when
            val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                Position(square("e4"), whiteKnight()),
                gameState
            )
            // Then
            expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder(
                "g5", "g3", "c5", "c3", "f6", "f2", "d6", "d2"
            )
            expectThat(legalMoves.contains(
                Move(
                    piece = whiteKnight(),
                    from = square("e4"),
                    destination = square("g5"),
                    capturedPiece = blackBishop(),
                )
            )).isTrue()
        }
    }

    @Nested
    inner class Queen {
        @Test
        fun `should return all legal squares when there is no piece on the way`() {
            // given
            val gameState =  givenAChessGame(
                fenPiecePlacementOnly = "8/8/8/8/8/8/1Q6/8",
            )
            // when
            val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                Position(square("b2"), whiteQueen()),
                gameState
            )
            // then
            expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder(
                "a2", "c2", "d2", "e2", "f2", "g2", "h2",
                "b1", "b3", "b4", "b5", "b6", "b7", "b8",
                "a1", "c3", "d4", "e5", "f6", "g7", "h8",
                "a3", "c1"
            )
        }

        @Test
        fun `should return all legal squares when there is one piece of the same color on the way`() {
            // given
            val gameState =  givenAChessGame(
                fenPiecePlacementOnly = "8/1P6/8/8/8/8/1Q6/8",
            )
            // when
            val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                Position(square("b2"), whiteQueen()),
                gameState
            )
            // then
            expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder(
                "a2", "c2", "d2", "e2", "f2", "g2", "h2",
                "b1", "b3", "b4", "b5", "b6",
                "a1", "c3", "d4", "e5", "f6", "g7", "h8",
                "a3", "c1"
            )
        }

        @Test
        fun `should return all legal squares when there is one piece of the opposite color up on the way`() {
            // given
            val gameState =  givenAChessGame(
                fenPiecePlacementOnly = "8/1p6/8/8/8/8/1Q6/8",
            )
            // when
            val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                Position(square("b2"), whiteQueen()),
                gameState
            )
            // then
            expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder(
                "a2", "c2", "d2", "e2", "f2", "g2", "h2",
                "b1", "b3", "b4", "b5", "b6", "b7",
                "a1", "c3", "d4", "e5", "f6", "g7", "h8",
                "a3", "c1"
            )
            expectThat(legalMoves.contains(Move(
                piece = whiteQueen(),
                from = square("b2"),
                destination = square("b7"),
                capturedPiece = blackPawn(),
            )))
        }

        @Test
        fun `should return all legal squares when there is one piece of the opposite color diagonally on the way`() {
            // given
            val gameState =  givenAChessGame(
                fenPiecePlacementOnly = "8/8/5p2/8/8/8/1Q6/8",
            )
            // when
            val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                Position(square("b2"), whiteQueen()),
                gameState
            )
            // then
            expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder(
                "a2", "c2", "d2", "e2", "f2", "g2", "h2",
                "b1", "b3", "b4", "b5", "b6", "b7", "b8",
                "a1", "c3", "d4", "e5", "f6",
                "a3", "c1"
            )
            expectThat(legalMoves.contains(Move(
                piece = whiteQueen(),
                from = square("b2"),
                destination = square("f6"),
                capturedPiece = blackPawn(),
            )))
        }

        @Test
        fun `should return no legal squares when there is no space to move`() {
            // given
            val gameState =  givenAChessGame(
                fenPiecePlacementOnly = "8/8/8/8/8/8/PB6/QN6",
            )
            // when
            val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                Position(square("a1"), whiteQueen()),
                gameState
            )
            // then
            expectThat(legalMoves).isEmpty()
        }
    }

    @Nested
    inner class Rook {

        @Test
        fun `should return all legal squares when there is no piece on the way`() {
            // given
            val gameState =  givenAChessGame(
                fenPiecePlacementOnly = "8/8/8/8/8/8/1R6/8",
            )
            // when
            val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                Position(square("b2"), whiteRook()),
                gameState
            )
            // then
            expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder(
                "a2", "c2", "d2", "e2", "f2", "g2", "h2",
                "b1", "b3", "b4", "b5", "b6", "b7", "b8",
            )
        }

        @Test
        fun `should return all legal squares when there is one piece of the same color on the way`() {
            // given
            val gameState =  givenAChessGame(
                fenPiecePlacementOnly = "8/1P6/8/8/8/8/1R6/8",
            )
            // when
            val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                Position(square("b2"), whiteRook()),
                gameState
            )
            // then
            expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder(
                "a2", "c2", "d2", "e2", "f2", "g2", "h2",
                "b1", "b3", "b4", "b5", "b6",
            )
        }

        @Test
        fun `should return all legal squares when there is one piece of the opposite color on the way`() {
            // given
            val gameState =  givenAChessGame(
                fenPiecePlacementOnly = "8/1p6/8/8/8/8/1R6/8",
            )
            // when
            val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                Position(square("b2"), whiteRook()),
                gameState
            )
            // then
            expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder(
                "a2", "c2", "d2", "e2", "f2", "g2", "h2",
                "b1", "b3", "b4", "b5", "b6", "b7"
            )
            expectThat(legalMoves.contains(Move(
                piece = whiteRook(),
                from = square("b2"),
                destination = square("b7"),
                capturedPiece = blackPawn(),
            )))
        }

        @Test
        fun `should return no legal squares when there is no space to move`() {
            // given
            val gameState =  givenAChessGame(
                fenPiecePlacementOnly = "8/8/8/8/8/8/PB6/RN6",
            )
            // when
            val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                Position(square("a1"), whiteQueen()),
                gameState
            )
            // then
            expectThat(legalMoves).isEmpty()
        }
    }

    @Nested
    inner class Bishop {

        @Test
        fun `should return all legal squares when there is no piece on the way`() {
            // given
            val gameState =  givenAChessGame(
                fenPiecePlacementOnly = "8/8/8/8/8/8/1B6/8",
            )
            // when
            val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                Position(square("b2"), whiteBishop()),
                gameState
            )
            // then
            expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder(
                "a1", "c3", "d4", "e5", "f6", "g7", "h8",
                "a3", "c1"
            )
        }

        @Test
        fun `should return all legal squares when there is one piece of the same color on the way`() {
            // given
            val gameState =  givenAChessGame(
                fenPiecePlacementOnly = "8/8/8/8/8/2P5/1B6/8",
            )
            // when
            val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                Position(square("b2"), whiteBishop()),
                gameState
            )
            // then
            expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder(
                "a1", "a3", "c1"
            )
        }

        @Test
        fun `should return all legal squares when there is one piece of the opposite color on the way`() {
            // given
            val gameState =  givenAChessGame(
                fenPiecePlacementOnly = "8/8/8/8/8/2p5/1B6/8",
            )
            // when
            val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                Position(square("b2"), whiteBishop()),
                gameState
            )
            // then
            expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder(
                "a1", "c3", "a3", "c1"
            )
            expectThat(legalMoves.contains(Move(
                piece = whiteBishop(),
                from = square("b2"),
                destination = square("c3"),
                capturedPiece = blackPawn(),
            )))
        }

        @Test
        fun `should return no legal squares when there is no space to move`() {
            // given
            val gameState =  givenAChessGame(
                fenPiecePlacementOnly = "8/8/8/8/8/8/1P6/B7",
            )
            // when
            val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                Position(square("a1"), whiteBishop()),
                gameState
            )
            // then
            expectThat(legalMoves).isEmpty()
        }
    }

    @Nested
    inner class King {

        @Nested
        inner class Castling {

            @Nested
            inner class White {
                @Test
                fun `should return white king castle move when it is possible`() {
                    // given
                    val gameState =  givenAChessGame(
                        fenPiecePlacementOnly = "8/8/8/8/8/8/8/R3K2R",
                    )
                    // when
                    val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                        Position(square("e1"), whiteKing()),
                        gameState
                    )
                    // then
                    expectThat(legalMoves.contains(
                        Move(
                            piece = whiteKing(),
                            from = square("e1"),
                            destination = square("g1"),
                            isQueenCastle = false,
                            isKingCastle = true,
                        )
                    ))
                }

                @Test
                fun `should return white queen castle move when it is possible`() {
                    // given
                    val gameState =  givenAChessGame(
                        fenPiecePlacementOnly = "8/8/8/8/8/8/8/R3K2R",
                    )
                    // when
                    val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                        Position(square("e1"), whiteKing()),
                        gameState
                    )
                    // then
                    expectThat(legalMoves.contains(
                        Move(
                            piece = whiteKing(),
                            from = square("e1"),
                            destination = square("c1"),
                            isQueenCastle = true,
                            isKingCastle = false,
                        )
                    ))
                }

                @Test
                fun `should not return white king castle move when there is piece between king and king rook`() {
                    // Given
                    val gameState =  givenAChessGame()
                    // When
                    val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                        Position(square("e1"), whiteKing()),
                        gameState
                    )
                    // Then
                    expectThat(legalMoves.none { it.isKingCastle }).isTrue()
                }

                @Test
                fun `should not return white queen castle move when there is piece between king and queen rook`() {
                    // Given
                    val gameState =  givenAChessGame()
                    // When
                    val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                        Position(square("e1"), whiteKing()),
                        gameState
                    )
                    // Then
                    expectThat(legalMoves.none { it.isQueenCastle }).isTrue()
                }

                @Test
                fun `should not return white king castle move when white king rook has moved previously`() {
                    // given
                    val gameState =  givenAChessGame(
                        fenPiecePlacementOnly = "8/8/8/8/8/8/8/R3K2R",
                        isWhiteKingCastlePossible = false,
                        isWhiteQueenCastlePossible = true,
                    )
                    // when
                    val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                        Position(square("e1"), whiteKing()),
                        gameState
                    )
                    // Then
                    expectThat(legalMoves.none { it.isKingCastle }).isTrue()
                }

                @Test
                fun `should not return white queen castle move when white queen rook has moved previously`() {
                    // given
                    val gameState =  givenAChessGame(
                        fenPiecePlacementOnly = "8/8/8/8/8/8/8/R3K2R",
                        isWhiteKingCastlePossible = true,
                        isWhiteQueenCastlePossible = false,
                    )
                    // when
                    val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                        Position(square("e1"), whiteKing()),
                        gameState
                    )
                    // Then
                    expectThat(legalMoves.none { it.isQueenCastle}).isTrue()
                }
            }


            @Nested
            inner class Black {
                @Test
                fun `should return black king castle move when it is possible`() {
                    // given
                    val gameState =  givenAChessGame(
                        fenPiecePlacementOnly = "r3k2r/8/8/8/8/8/8/8",
                    )
                    // when
                    val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                        Position(square("e8"), whiteKing()),
                        gameState
                    )
                    // then
                    expectThat(legalMoves.contains(
                        Move(
                            piece = whiteKing(),
                            from = square("e8"),
                            destination = square("g8"),
                            isKingCastle = true,
                            isQueenCastle = false,
                        )
                    ))
                }

                @Test
                fun `should return black queen castle move when it is possible`() {
                    // given
                    val gameState =  givenAChessGame(
                        fenPiecePlacementOnly = "r3k2r/8/8/8/8/8/8/8",
                    )
                    // when
                    val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                        Position(square("e8"), whiteKing()),
                        gameState
                    )
                    // then
                    expectThat(legalMoves.contains(
                        Move(
                            piece = whiteKing(),
                            from = square("e8"),
                            destination = square("c8"),
                            isKingCastle = true,
                            isQueenCastle = false,
                        )
                    ))
                }

                @Test
                fun `should not return black king castle move when there is piece between king and king rook`() {
                    // given
                    val gameState =  givenAChessGame()
                    // when
                    val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                        Position(square("e8"), blackKing()),
                        gameState
                    )
                    // Then
                    expectThat(legalMoves.none { it.isKingCastle }).isTrue()
                }

                @Test
                fun `should not return black queen castle move when there is piece between king and queen rook`() {
                    // given
                    val gameState =  givenAChessGame()
                    // when
                    val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                        Position(square("e8"), blackKing()),
                        gameState
                    )
                    // Then
                    expectThat(legalMoves.none { it.isQueenCastle }).isTrue()
                }
            }



            @Test
            fun `should not return castle move when king is currently checked`() {
                // given
                val gameState =  givenAChessGame(
                    fenPiecePlacementOnly = "r3k2r/8/8/8/4R3/8/8/8"
                )
                // when
                val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                    Position(square("e8"), blackKing()),
                    gameState
                )
                // Then
                expectThat(legalMoves.none { it.isQueenCastle || it.isKingCastle }).isTrue()
            }

            @Test
            fun `should not return castle move when king is checked in a square during movement`() {
                // given
                val gameState =  givenAChessGame(
                    fenPiecePlacementOnly = "r3k2r/8/8/8/5R2/8/8/8"
                )
                // when
                val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                    Position(square("e8"), blackKing()),
                    gameState
                )
                // Then
                expectThat(legalMoves.none { it.isKingCastle }).isTrue()
            }

            @Test
            fun `should not return castle move when king is checked at the end`() {
                // given
                val gameState =  givenAChessGame(
                    fenPiecePlacementOnly = "r3k2r/8/8/8/6R1/8/8/8"
                )
                // when
                val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                    Position(square("e8"), blackKing()),
                    gameState
                )
                // Then
                expectThat(legalMoves.none { it.isKingCastle }).isTrue()
            }

            @Test
            fun `should not return castle move when king has move previously`() {
                // given
                val gameState =  givenAChessGame(
                    fenPiecePlacementOnly = "r3k2r/8/8/8/6R1/8/8/8"
                )
                // when
                val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                    Position(square("e8"), blackKing()),
                    gameState
                )
                // Then
                expectThat(legalMoves.none { it.isKingCastle }).isTrue()
            }

            @Test
            fun `should not return black king castle move when black king rook has moved previously`() {
                // given
                val gameState =  givenAChessGame(
                    fenPiecePlacementOnly = "r3k2r/8/8/8/8/8/8/8",
                    isBlackKingCastlePossible = false,
                    isBlackQueenCastlePossible = true,
                )
                // when
                val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                    Position(square("e8"), blackKing()),
                    gameState
                )
                // Then
                expectThat(legalMoves.none { it.isKingCastle }).isTrue()
            }

            @Test
            fun `should not return black queen castle move when black queen rook has moved previously`() {
                // given
                val gameState =  givenAChessGame(
                    fenPiecePlacementOnly = "r3k2r/8/8/8/8/8/8/8",
                    isBlackKingCastlePossible = true,
                    isBlackQueenCastlePossible = false,
                )
                // when
                val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                    Position(square("e8"), blackKing()),
                    gameState
                )
                // Then
                expectThat(legalMoves.none { it.isQueenCastle }).isTrue()
            }
        }

        @Test
        fun `should return all moves when no pieces around`() {
            // given
            val gameState =  givenAChessGame(
                fenPiecePlacementOnly = "8/8/8/8/3K4/8/8/8",
                isBlackKingCastlePossible = false,
                isBlackQueenCastlePossible = false,
                isWhiteQueenCastlePossible = false,
                isWhiteKingCastlePossible = false,
            )
            // when
            val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                Position(square("d4"), whiteKing()),
                gameState
            )
            // then
            expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder(
                "c5", "d5", "e5", "c4", "e4", "c3", "d3", "e3"
            )
        }

        @Test
        fun `should return all moves when king takes an opposite piece`() {
            // given
            val gameState =  givenAChessGame(
                fenPiecePlacementOnly = "8/8/8/4p3/3K4/8/8/8",
                isBlackKingCastlePossible = false,
                isBlackQueenCastlePossible = false,
                isWhiteQueenCastlePossible = false,
                isWhiteKingCastlePossible = false,
            )
            // when
            val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                Position(square("d4"), whiteKing()),
                gameState
            )
            // then
            expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder(
                "c5", "d5", "e5", "c4", "e4", "c3", "d3", "e3"
            )
            expectThat(legalMoves.contains(Move(
                piece = whiteKing(),
                from = square("d4"),
                destination = square("e5"),
                capturedPiece = blackPawn(),
            ))).isTrue()
        }

        @Test
        fun `should return all moves when there is a piece from same color around`() {
            // given
            val gameState =  givenAChessGame(
                fenPiecePlacementOnly = "8/8/8/8/8/8/P7/K7",
                isBlackKingCastlePossible = false,
                isBlackQueenCastlePossible = false,
                isWhiteQueenCastlePossible = false,
                isWhiteKingCastlePossible = false,
            )
            // when
            val legalMoves = pseudoLegalMovesFinder.getAllPseudoLegalMoves(
                Position(square("a1"), whiteKing()),
                gameState
            )
            // then
            expectThat(legalMoves.map { it.destination.toString() }).containsExactlyInAnyOrder(
                "b1", "b2"
            )
        }
    }
}