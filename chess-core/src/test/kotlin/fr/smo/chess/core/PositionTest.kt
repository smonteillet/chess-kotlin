package fr.smo.chess.core

import fr.smo.chess.core.Color.BLACK
import fr.smo.chess.core.Color.WHITE
import fr.smo.chess.core.Piece.*
import fr.smo.chess.core.PieceType.*
import fr.smo.chess.core.Square.*
import fr.smo.chess.core.fixtures.GameStateFixtures.givenAChessGame
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

class PositionTest {

    @Nested
    inner class GetAllPseudoLegalMoves {

        @Nested
        inner class Pawn {

            @Nested
            inner class White {
                @Test
                fun `should return a one square move and no two squares move when a piece is blocking the two squares move`() {
                    // given
                    val game = givenAChessGame(
                        fenPiecePlacementOnly = "8/8/8/8/1P6/8/1P6/8",
                    )
                    // when
                    val legalMoves = Position(B2, WHITE_PAWN).getAllPseudoLegalMoves(game)
                    // then
                    expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(B3)
                }

                @Test
                fun `should return no moves when a piece with the same color is blocking one square away`() {
                    // given
                    val game = givenAChessGame(
                        fenPiecePlacementOnly = "8/8/8/8/8/1P6/1P6/8",
                    )
                    // when
                    val legalMoves = Position(B2, WHITE_PAWN).getAllPseudoLegalMoves(game)
                    // then
                    expectThat(legalMoves).isEmpty()
                }

                @Test
                fun `should return no moves when a piece with opposite color is blocking one square away`() {
                    // given
                    val game = givenAChessGame(
                        fenPiecePlacementOnly = "8/8/8/8/8/1p6/1P6/8",
                    )
                    // when
                    val legalMoves = Position(B2, WHITE_PAWN).getAllPseudoLegalMoves(game)
                    // then
                    expectThat(legalMoves).isEmpty()
                }

                @Test
                fun `should return a one square move and a two square moves when possible`() {
                    // given
                    val game = givenAChessGame(
                        fenPiecePlacementOnly = "8/8/8/8/8/8/1P6/8",
                    )
                    // when
                    val legalMoves = Position(B2, WHITE_PAWN).getAllPseudoLegalMoves(game)
                    // then
                    expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(B3, B4)
                }

                @Test
                fun `should not return a two squares move if pawn has previously moved`() {
                    // given
                    val game = givenAChessGame(
                        fenPiecePlacementOnly = "8/8/8/8/8/1P6/8/8",
                    )
                    // when
                    val legalMoves = Position(B3, WHITE_PAWN).getAllPseudoLegalMoves(game)
                    // then
                    expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(B4)
                }

                @Test
                fun `should capture black piece at right diagonal move when possible`() {
                    // given
                    val game = givenAChessGame(
                        fenPiecePlacementOnly = "8/8/8/8/2p5/1P6/8/8",
                    )
                    // when
                    val legalMoves = Position(B3, WHITE_PAWN).getAllPseudoLegalMoves(game)

                    // then
                    expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(B4, C4)
                    expectThat(
                        legalMoves.contains(
                            Move(
                                piece = WHITE_PAWN,
                                from = B3,
                                destination = C4,
                                capturedPiece = BLACK_PAWN,
                            )
                        )
                    ).isTrue()
                }


                @Test
                fun `should capture black piece at left diagonal move when possible`() {
                    // given
                    val game = givenAChessGame(
                        fenPiecePlacementOnly = "8/8/8/8/p7/1P6/8/8",
                    )
                    // when
                    val legalMoves = Position(B3, WHITE_PAWN).getAllPseudoLegalMoves(game)
                    // then
                    expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(B4, A4)
                    expectThat(
                        legalMoves.contains(
                            Move(
                                piece = WHITE_PAWN,
                                from = B3,
                                destination = A4,
                                capturedPiece = BLACK_PAWN,
                            )
                        )
                    ).isTrue()
                }

                @Test
                fun `should capture black piece at left diagonal and right diagonal moves when possible`() {
                    // given
                    val game = givenAChessGame(
                        fenPiecePlacementOnly = "8/8/8/8/p1p5/1P6/8/8",
                    )
                    // when
                    val legalMoves = Position(B3, WHITE_PAWN).getAllPseudoLegalMoves(game)
                    // then
                    expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(B4, A4, C4)
                    expectThat(
                        legalMoves.contains(
                            Move(
                                piece = WHITE_PAWN,
                                from = B3,
                                destination = A4,
                                capturedPiece = BLACK_PAWN,
                            )
                        )
                    ).isTrue()
                    expectThat(
                        legalMoves.contains(
                            Move(
                                piece = WHITE_PAWN,
                                from = B3,
                                destination = C4,
                                capturedPiece = BLACK_PAWN,
                            )
                        )
                    ).isTrue()
                }

                @Test
                fun `should promote when possible`() {
                    // given
                    val game = givenAChessGame(
                        fenPiecePlacementOnly = "8/2P5/8/8/8/8/8/8",
                    )
                    // when
                    val legalMoves = Position(C7, WHITE_PAWN).getAllPseudoLegalMoves(game)
                    // Then
                    expectThat(legalMoves.map { it.promotedTo!!.type }).containsExactlyInAnyOrder(
                        QUEEN,
                        ROOK,
                        BISHOP,
                        KNIGHT
                    )
                    expectThat(legalMoves.map { it.promotedTo!!.color }.all { it == WHITE }) isEqualTo true
                    expectThat(legalMoves.map { it.destination }.all { it == C8 }) isEqualTo true
                }

                @Nested
                inner class EnPassant {

                    @Test
                    fun `should take en passant to the left when possible`() {
                        // Given
                        val game = givenAChessGame(
                            fenPiecePlacementOnly = "8/8/8/2pP4/8/8/8/8",
                            enPassantTargetSquare = C6
                        )
                        // when
                        val legalMoves = Position(D5, WHITE_PAWN).getAllPseudoLegalMoves(game)
                        // Then
                        expectThat(legalMoves).containsExactlyInAnyOrder(
                            Move(WHITE_PAWN, D5, D6),
                            Move(WHITE_PAWN, D5, C6, capturedPiece = BLACK_PAWN)
                        )
                    }

                    @Test
                    fun `should take en passant to the right when possible`() {
                        // Given
                        val game = givenAChessGame(
                            fenPiecePlacementOnly = "8/8/8/3Pp3/8/8/8/8",
                            enPassantTargetSquare = E6
                        )
                        // when
                        val legalMoves = Position(D5, WHITE_PAWN).getAllPseudoLegalMoves(game)
                        // Then
                        expectThat(legalMoves).containsExactlyInAnyOrder(
                            Move(WHITE_PAWN, D5, D6),
                            Move(WHITE_PAWN, D5, E6, capturedPiece = BLACK_PAWN)
                        )
                    }

                    @Test
                    fun `should not take en passant when opposite piece is not a pawn on row 6`() {
                        // Given
                        val game = givenAChessGame(
                            fenPiecePlacementOnly = "8/8/8/2bP4/8/8/8/8"
                        )
                        // when
                        val legalMoves = Position(D5, WHITE_PAWN).getAllPseudoLegalMoves(game)
                        // Then
                        expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(D6)
                    }
                }
            }

            @Nested
            inner class Black {

                @Test
                fun `should return a one square move and no two squares move when a piece is blocking the two squares move`() {
                    // Given
                    val game = givenAChessGame(
                        fenPiecePlacementOnly = "8/1p6/8/1p6/8/8/8/8"
                    )
                    // when
                    val legalMoves = Position(B7, BLACK_PAWN).getAllPseudoLegalMoves(game)
                    // then
                    expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(B6)
                }

                @Test
                fun `should return no moves when a piece with the same color is blocking one square away`() {
                    // Given
                    val game = givenAChessGame(
                        fenPiecePlacementOnly = "8/1p6/1p6/8/8/8/8/8"
                    )
                    // when
                    val legalMoves = Position(B7, BLACK_PAWN).getAllPseudoLegalMoves(game)
                    // then
                    expectThat(legalMoves).isEmpty()
                }

                @Test
                fun `should return no moves when a piece with opposite color is blocking one square away`() {
                    // Given
                    val game = givenAChessGame(
                        fenPiecePlacementOnly = "8/1p6/1P6/8/8/8/8/8"
                    )
                    // when
                    val legalMoves = Position(B7, BLACK_PAWN).getAllPseudoLegalMoves(game)
                    // then
                    expectThat(legalMoves).isEmpty()
                }

                @Test
                fun `should return a one square move and a two square moves when possible`() {
                    // Given
                    val game = givenAChessGame(
                        fenPiecePlacementOnly = "8/1p6/8/8/8/8/8/8"
                    )
                    // when
                    val legalMoves = Position(B7, BLACK_PAWN).getAllPseudoLegalMoves(game)
                    // then
                    expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(B6, B5)
                }

                @Test
                fun `should not return a two squares move if pawn has previously moved`() {
                    // Given
                    val game = givenAChessGame(
                        fenPiecePlacementOnly = "8/8/1p6/8/8/8/8/8"
                    )
                    // when
                    val legalMoves = Position(B6, BLACK_PAWN).getAllPseudoLegalMoves(game)
                    // then
                    expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(B5)
                }

                @Test
                fun `should capture white pawn at right diagonal  when possible`() {
                    // Given
                    val game = givenAChessGame(
                        fenPiecePlacementOnly = "8/8/1p6/2P5/8/8/8/8"
                    )
                    // when
                    val legalMoves = Position(B6, BLACK_PAWN).getAllPseudoLegalMoves(game)
                    // then
                    expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(B5, C5)
                    expectThat(
                        legalMoves.contains(
                            Move(
                                piece = BLACK_PAWN,
                                from = B6,
                                destination = C5,
                                capturedPiece = WHITE_PAWN,
                            )
                        )
                    )
                }


                @Test
                fun `should capture white pawn at left diagonal  when possible`() {
                    // Given
                    val game = givenAChessGame(
                        fenPiecePlacementOnly = "8/8/1p6/P7/8/8/8/8"
                    )
                    // when
                    val legalMoves = Position(B6, BLACK_PAWN).getAllPseudoLegalMoves(game)
                    // then
                    expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(B5, A5)
                    expectThat(
                        legalMoves.contains(
                            Move(
                                piece = BLACK_PAWN,
                                from = B6,
                                destination = A5,
                                capturedPiece = WHITE_PAWN,
                            )
                        )
                    )
                }

                @Test
                fun `should capture piece at left diagonal and right diagonal when possible`() {
                    // Given
                    val game = givenAChessGame(
                        fenPiecePlacementOnly = "8/8/1p6/P1P5/8/8/8/8"
                    )
                    // when
                    val legalMoves = Position(B6, BLACK_PAWN).getAllPseudoLegalMoves(game)
                    // then
                    expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(B5, A5, C5)
                    expectThat(
                        legalMoves.contains(
                            Move(
                                piece = BLACK_PAWN,
                                from = B6,
                                destination = A5,
                                capturedPiece = WHITE_PAWN,
                            )
                        )
                    )
                    expectThat(
                        legalMoves.contains(
                            Move(
                                piece = BLACK_PAWN,
                                from = B6,
                                destination = C5,
                                capturedPiece = WHITE_PAWN,
                            )
                        )
                    )
                }

                @Test
                fun `should promote when possible`() {
                    // Given
                    val game = givenAChessGame(
                        fenPiecePlacementOnly = "8/8/8/8/8/8/1p6/8"
                    )
                    // when
                    val legalMoves = Position(B2, BLACK_PAWN).getAllPseudoLegalMoves(game)
                    // Then
                    expectThat(legalMoves.map { it.promotedTo!!.type }).containsExactlyInAnyOrder(
                        QUEEN,
                        ROOK,
                        BISHOP,
                        KNIGHT
                    )
                    expectThat(legalMoves.map { it.promotedTo!!.color }.all { it == BLACK }) isEqualTo true
                    expectThat(legalMoves.map { it.destination }.all { it == B1 }) isEqualTo true
                }

                @Nested
                inner class EnPassant {

                    @Test
                    fun `should take en passant to the left when possible`() {
                        // Given
                        val game = givenAChessGame(
                            fenPiecePlacementOnly = "8/8/8/8/3Pp3/8/8/8",
                            enPassantTargetSquare = D3,
                        )
                        // when
                        val legalMoves = Position(E4, BLACK_PAWN).getAllPseudoLegalMoves(game)
                        expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(D3, E3)
                        expectThat(
                            legalMoves.contains(
                                Move(
                                    piece = BLACK_PAWN,
                                    from = E4,
                                    destination = D3,
                                    capturedPiece = WHITE_PAWN,
                                )
                            )
                        ).isTrue()
                    }

                    @Test
                    fun `should take en passant to the right when possible`() {
                        // Given
                        val game = givenAChessGame(
                            fenPiecePlacementOnly = "8/8/8/8/2pP4/8/8/8",
                            enPassantTargetSquare = D3,
                        )
                        // when
                        val legalMoves = Position(C4, BLACK_PAWN).getAllPseudoLegalMoves(game)
                        expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(D3, C3)
                        expectThat(
                            legalMoves.contains(
                                Move(
                                    piece = BLACK_PAWN,
                                    from = C4,
                                    destination = D3,
                                    capturedPiece = WHITE_PAWN,
                                )
                            )
                        ).isTrue()
                    }

                    @Test
                    fun `should not take en passant when opposite piece is not a pawn on row 6`() {
                        // Given
                        val game = givenAChessGame(
                            fenPiecePlacementOnly = "8/8/8/8/2Bp4/8/8/8",
                            enPassantTargetSquare = null,
                        )
                        // when
                        val legalMoves = Position(D4, BLACK_PAWN).getAllPseudoLegalMoves(game)
                        expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(D3)
                    }

                    @Test
                    fun `should not take en passant when opposite pawn on raw 6 has not made a two square move at first`() {
                        // Given
                        val game = givenAChessGame(
                            fenPiecePlacementOnly = "8/8/8/8/2Pp4/8/8/8",
                            enPassantTargetSquare = null,
                        )
                        // when
                        val legalMoves = Position(D4, BLACK_PAWN).getAllPseudoLegalMoves(game)
                        expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(D3)
                    }
                }
            }

        }

        @Nested
        inner class Knight {
            @Test
            fun `should return all 8 knight moves when possible`() {
                // given
                val game = givenAChessGame(
                    fenPiecePlacementOnly = "8/8/8/8/4N3/8/8/8",
                )
                // when
                val legalMoves = Position(E4, WHITE_KNIGHT).getAllPseudoLegalMoves(game)
                // then
                expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(
                    G5, G3, C5, C3, F6, F2, D6, D2
                )
            }

            @Test
            fun `should not return out of bound moves`() {
                // given
                val game = givenAChessGame(
                    fenPiecePlacementOnly = "8/8/8/8/8/8/8/N7",
                )
                // when
                val legalMoves = Position(A1, WHITE_KNIGHT).getAllPseudoLegalMoves(game)
                // then
                expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(
                    C2, B3
                )
            }

            @Test
            fun `should not move to square where it has a same color piece`() {
                // given
                val game = givenAChessGame(
                    fenPiecePlacementOnly = "8/8/8/6B1/4N3/8/8/8",
                )
                // when
                val legalMoves = Position(E4, WHITE_KNIGHT).getAllPseudoLegalMoves(game)
                // Then
                expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(
                    G3, C5, C3, F6, F2, D6, D2
                )
            }

            @Test
            fun `should move to square where it can take a piece`() {
                // given
                val game = givenAChessGame(
                    fenPiecePlacementOnly = "8/8/8/6b1/4N3/8/8/8",
                )
                // when
                val legalMoves = Position(E4, WHITE_KNIGHT).getAllPseudoLegalMoves(game)
                // Then
                expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(
                    G5, G3, C5, C3, F6, F2, D6, D2
                )
                expectThat(
                    legalMoves.contains(
                        Move(
                            piece = WHITE_KNIGHT,
                            from = E4,
                            destination = G5,
                            capturedPiece = BLACK_BISHOP,
                        )
                    )
                ).isTrue()
            }
        }

        @Nested
        inner class Queen {
            @Test
            fun `should return all legal squares when there is no piece on the way`() {
                // given
                val game = givenAChessGame(
                    fenPiecePlacementOnly = "8/8/8/8/8/8/1Q6/8",
                )
                // when
                val legalMoves = Position(B2, WHITE_QUEEN).getAllPseudoLegalMoves(game)
                // then
                expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(
                    A2, C2, D2, E2, F2, G2, H2,
                    B1, B3, B4, B5, B6, B7, B8,
                    A1, C3, D4, E5, F6, G7, H8,
                    A3, C1
                )
            }

            @Test
            fun `should return all legal squares when there is one piece of the same color on the way`() {
                // given
                val game = givenAChessGame(
                    fenPiecePlacementOnly = "8/1P6/8/8/8/8/1Q6/8",
                )
                // when
                val legalMoves = Position(B2, WHITE_QUEEN).getAllPseudoLegalMoves(game)
                // then
                expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(
                    A2, C2, D2, E2, F2, G2, H2,
                    B1, B3, B4, B5, B6,
                    A1, C3, D4, E5, F6, G7, H8,
                    A3, C1
                )
            }

            @Test
            fun `should return all legal squares when there is one piece of the opposite color up on the way`() {
                // given
                val game = givenAChessGame(
                    fenPiecePlacementOnly = "8/1p6/8/8/8/8/1Q6/8",
                )
                // when
                val legalMoves = Position(B2, WHITE_QUEEN).getAllPseudoLegalMoves(game)
                // then
                expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(
                    A2, C2, D2, E2, F2, G2, H2,
                    B1, B3, B4, B5, B6, B7,
                    A1, C3, D4, E5, F6, G7, H8,
                    A3, C1
                )
                expectThat(
                    legalMoves.contains(
                        Move(
                            piece = WHITE_QUEEN,
                            from = B2,
                            destination = B7,
                            capturedPiece = BLACK_PAWN,
                        )
                    )
                )
            }

            @Test
            fun `should return all legal squares when there is one piece of the opposite color diagonally on the way`() {
                // given
                val game = givenAChessGame(
                    fenPiecePlacementOnly = "8/8/5p2/8/8/8/1Q6/8",
                )
                // when
                val legalMoves = Position(B2, WHITE_QUEEN).getAllPseudoLegalMoves(game)
                // then
                expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(
                    A2, C2, D2, E2, F2, G2, H2,
                    B1, B3, B4, B5, B6, B7, B8,
                    A1, C3, D4, E5, F6,
                    A3, C1
                )
                expectThat(
                    legalMoves.contains(
                        Move(
                            piece = WHITE_QUEEN,
                            from = B2,
                            destination = F6,
                            capturedPiece = BLACK_PAWN,
                        )
                    )
                )
            }

            @Test
            fun `should return no legal squares when there is no space to move`() {
                // given
                val game = givenAChessGame(
                    fenPiecePlacementOnly = "8/8/8/8/8/8/PB6/QN6",
                )
                // when
                val legalMoves = Position(A1, WHITE_QUEEN).getAllPseudoLegalMoves(game)
                // then
                expectThat(legalMoves).isEmpty()
            }
        }

        @Nested
        inner class Rook {

            @Test
            fun `should return all legal squares when there is no piece on the way`() {
                // given
                val game = givenAChessGame(
                    fenPiecePlacementOnly = "8/8/8/8/8/8/1R6/8",
                )
                // when
                val legalMoves = Position(B2, WHITE_ROOK).getAllPseudoLegalMoves(game)
                // then
                expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(
                    A2, C2, D2, E2, F2, G2, H2,
                    B1, B3, B4, B5, B6, B7, B8,
                )
            }

            @Test
            fun `should return all legal squares when there is one piece of the same color on the way`() {
                // given
                val game = givenAChessGame(
                    fenPiecePlacementOnly = "8/1P6/8/8/8/8/1R6/8",
                )
                // when
                val legalMoves = Position(B2, WHITE_ROOK).getAllPseudoLegalMoves(game)
                // then
                expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(
                    A2, C2, D2, E2, F2, G2, H2,
                    B1, B3, B4, B5, B6,
                )
            }

            @Test
            fun `should return all legal squares when there is one piece of the opposite color on the way`() {
                // given
                val game = givenAChessGame(
                    fenPiecePlacementOnly = "8/1p6/8/8/8/8/1R6/8",
                )
                // when
                val legalMoves = Position(B2, WHITE_ROOK).getAllPseudoLegalMoves(game)
                // then
                expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(
                    A2, C2, D2, E2, F2, G2, H2,
                    B1, B3, B4, B5, B6, B7
                )
                expectThat(
                    legalMoves.contains(
                        Move(
                            piece = WHITE_ROOK,
                            from = B2,
                            destination = B7,
                            capturedPiece = BLACK_PAWN,
                        )
                    )
                )
            }

            @Test
            fun `should return no legal squares when there is no space to move`() {
                // given
                val game = givenAChessGame(
                    fenPiecePlacementOnly = "8/8/8/8/8/8/PB6/RN6",
                )
                // when
                val legalMoves = Position(A1, WHITE_QUEEN).getAllPseudoLegalMoves(game)
                // then
                expectThat(legalMoves).isEmpty()
            }
        }

        @Nested
        inner class Bishop {

            @Test
            fun `should return all legal squares when there is no piece on the way`() {
                // given
                val game = givenAChessGame(
                    fenPiecePlacementOnly = "8/8/8/8/8/8/1B6/8",
                )
                // when
                val legalMoves = Position(B2, WHITE_BISHOP).getAllPseudoLegalMoves(game)
                // then
                expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(
                    A1, C3, D4, E5, F6, G7, H8,
                    A3, C1
                )
            }

            @Test
            fun `should return all legal squares when there is one piece of the same color on the way`() {
                // given
                val game = givenAChessGame(
                    fenPiecePlacementOnly = "8/8/8/8/8/2P5/1B6/8",
                )
                // when
                val legalMoves = Position(B2, WHITE_BISHOP).getAllPseudoLegalMoves(game)
                // then
                expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(
                    A1, A3, C1
                )
            }

            @Test
            fun `should return all legal squares when there is one piece of the opposite color on the way`() {
                // given
                val game = givenAChessGame(
                    fenPiecePlacementOnly = "8/8/8/8/8/2p5/1B6/8",
                )
                // when
                val legalMoves = Position(B2, WHITE_BISHOP).getAllPseudoLegalMoves(game)
                // then
                expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(
                    A1, C3, A3, C1
                )
                expectThat(
                    legalMoves.contains(
                        Move(
                            piece = WHITE_BISHOP,
                            from = B2,
                            destination = C3,
                            capturedPiece = BLACK_PAWN,
                        )
                    )
                )
            }

            @Test
            fun `should return no legal squares when there is no space to move`() {
                // given
                val game = givenAChessGame(
                    fenPiecePlacementOnly = "8/8/8/8/8/8/1P6/B7",
                )
                // when
                val legalMoves = Position(A1, WHITE_BISHOP).getAllPseudoLegalMoves(game)
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
                        val game = givenAChessGame(
                            fenPiecePlacementOnly = "8/8/8/8/8/8/8/R3K2R",
                        )
                        // when
                        val legalMoves = Position(E1, WHITE_KING).getAllPseudoLegalMoves(game)
                        // then
                        expectThat(
                            legalMoves.contains(
                                Move(
                                    piece = WHITE_KING,
                                    from = E1,
                                    destination = G1,
                                    isQueenCastle = false,
                                    isKingCastle = true,
                                )
                            )
                        )
                    }

                    @Test
                    fun `should return white queen castle move when it is possible`() {
                        // given
                        val game = givenAChessGame(
                            fenPiecePlacementOnly = "8/8/8/8/8/8/8/R3K2R",
                        )
                        // when
                        val legalMoves = Position(E1, WHITE_KING).getAllPseudoLegalMoves(game)
                        // then
                        expectThat(
                            legalMoves.contains(
                                Move(
                                    piece = WHITE_KING,
                                    from = E1,
                                    destination = C1,
                                    isQueenCastle = true,
                                    isKingCastle = false,
                                )
                            )
                        )
                    }

                    @Test
                    fun `should not return white king castle move when there is piece between king and king rook`() {
                        // Given
                        val game = givenAChessGame()
                        // When
                        val legalMoves = Position(E1, WHITE_KING).getAllPseudoLegalMoves(game)
                        // Then
                        expectThat(legalMoves.none { it.isKingCastle }).isTrue()
                    }

                    @Test
                    fun `should not return white queen castle move when there is piece between king and queen rook`() {
                        // Given
                        val game = givenAChessGame()
                        // When
                        val legalMoves = Position(E1, WHITE_KING).getAllPseudoLegalMoves(game)
                        // Then
                        expectThat(legalMoves.none { it.isQueenCastle }).isTrue()
                    }

                    @Test
                    fun `should not return white king castle move when white king rook has moved previously`() {
                        // given
                        val game = givenAChessGame(
                            fenPiecePlacementOnly = "8/8/8/8/8/8/8/R3K2R",
                            isWhiteKingCastlePossible = false,
                            isWhiteQueenCastlePossible = true,
                        )
                        // when
                        val legalMoves = Position(E1, WHITE_KING).getAllPseudoLegalMoves(game)
                        // Then
                        expectThat(legalMoves.none { it.isKingCastle }).isTrue()
                    }

                    @Test
                    fun `should not return white queen castle move when white queen rook has moved previously`() {
                        // given
                        val game = givenAChessGame(
                            fenPiecePlacementOnly = "8/8/8/8/8/8/8/R3K2R",
                            isWhiteKingCastlePossible = true,
                            isWhiteQueenCastlePossible = false,
                        )
                        // when
                        val legalMoves = Position(E1, WHITE_KING).getAllPseudoLegalMoves(game)
                        // Then
                        expectThat(legalMoves.none { it.isQueenCastle }).isTrue()
                    }
                }


                @Nested
                inner class Black {
                    @Test
                    fun `should return black king castle move when it is possible`() {
                        // given
                        val game = givenAChessGame(
                            fenPiecePlacementOnly = "r3k2r/8/8/8/8/8/8/8",
                        )
                        // when
                        val legalMoves = Position(E8, WHITE_KING).getAllPseudoLegalMoves(game)
                        // then
                        expectThat(
                            legalMoves.contains(
                                Move(
                                    piece = WHITE_KING,
                                    from = E8,
                                    destination = G8,
                                    isKingCastle = true,
                                    isQueenCastle = false,
                                )
                            )
                        )
                    }

                    @Test
                    fun `should return black queen castle move when it is possible`() {
                        // given
                        val game = givenAChessGame(
                            fenPiecePlacementOnly = "r3k2r/8/8/8/8/8/8/8",
                        )
                        // when
                        val legalMoves = Position(E8, WHITE_KING).getAllPseudoLegalMoves(game)
                        // then
                        expectThat(
                            legalMoves.contains(
                                Move(
                                    piece = WHITE_KING,
                                    from = E8,
                                    destination = C8,
                                    isKingCastle = true,
                                    isQueenCastle = false,
                                )
                            )
                        )
                    }

                    @Test
                    fun `should not return black king castle move when there is piece between king and king rook`() {
                        // given
                        val game = givenAChessGame()
                        // when
                        val legalMoves = Position(E8, BLACK_KING).getAllPseudoLegalMoves(game)
                        // Then
                        expectThat(legalMoves.none { it.isKingCastle }).isTrue()
                    }

                    @Test
                    fun `should not return black queen castle move when there is piece between king and queen rook`() {
                        // given
                        val game = givenAChessGame()
                        // when
                        val legalMoves = Position(E8, BLACK_KING).getAllPseudoLegalMoves(game)
                        // Then
                        expectThat(legalMoves.none { it.isQueenCastle }).isTrue()
                    }
                }


                @Test
                fun `should not return castle move when king is currently checked`() {
                    // given
                    val game = givenAChessGame(
                        fenPiecePlacementOnly = "r3k2r/8/8/8/4R3/8/8/8"
                    )
                    // when
                    val legalMoves = Position(E8, BLACK_KING).getAllPseudoLegalMoves(game)
                    // Then
                    expectThat(legalMoves.none { it.isQueenCastle || it.isKingCastle }).isTrue()
                }

                @Test
                fun `should not return castle move when king is checked in a square during movement`() {
                    // given
                    val game = givenAChessGame(
                        fenPiecePlacementOnly = "r3k2r/8/8/8/5R2/8/8/8"
                    )
                    // when
                    val legalMoves = Position(E8, BLACK_KING).getAllPseudoLegalMoves(game)
                    // Then
                    expectThat(legalMoves.none { it.isKingCastle }).isTrue()
                }

                @Test
                fun `should not return castle move when king is checked at the end`() {
                    // given
                    val game = givenAChessGame(
                        fenPiecePlacementOnly = "r3k2r/8/8/8/6R1/8/8/8"
                    )
                    // when
                    val legalMoves = Position(E8, BLACK_KING).getAllPseudoLegalMoves(game)
                    // Then
                    expectThat(legalMoves.none { it.isKingCastle }).isTrue()
                }

                @Test
                fun `should not return castle move when king has move previously`() {
                    // given
                    val game = givenAChessGame(
                        fenPiecePlacementOnly = "r3k2r/8/8/8/6R1/8/8/8"
                    )
                    // when
                    val legalMoves = Position(E8, BLACK_KING).getAllPseudoLegalMoves(game)
                    // Then
                    expectThat(legalMoves.none { it.isKingCastle }).isTrue()
                }

                @Test
                fun `should not return black king castle move when black king rook has moved previously`() {
                    // given
                    val game = givenAChessGame(
                        fenPiecePlacementOnly = "r3k2r/8/8/8/8/8/8/8",
                        isBlackKingCastlePossible = false,
                        isBlackQueenCastlePossible = true,
                    )
                    // when
                    val legalMoves = Position(E8, BLACK_KING).getAllPseudoLegalMoves(game)
                    // Then
                    expectThat(legalMoves.none { it.isKingCastle }).isTrue()
                }

                @Test
                fun `should not return black queen castle move when black queen rook has moved previously`() {
                    // given
                    val game = givenAChessGame(
                        fenPiecePlacementOnly = "r3k2r/8/8/8/8/8/8/8",
                        isBlackKingCastlePossible = true,
                        isBlackQueenCastlePossible = false,
                    )
                    // when
                    val legalMoves = Position(E8, BLACK_KING).getAllPseudoLegalMoves(game)
                    // Then
                    expectThat(legalMoves.none { it.isQueenCastle }).isTrue()
                }
            }

            @Test
            fun `should return all moves when no pieces around`() {
                // given
                val game = givenAChessGame(
                    fenPiecePlacementOnly = "8/8/8/8/3K4/8/8/8",
                    isBlackKingCastlePossible = false,
                    isBlackQueenCastlePossible = false,
                    isWhiteQueenCastlePossible = false,
                    isWhiteKingCastlePossible = false,
                )
                // when
                val legalMoves = Position(D4, WHITE_KING).getAllPseudoLegalMoves(game)
                // then
                expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(
                    C5, D5, E5, C4, E4, C3, D3, E3
                )
            }

            @Test
            fun `should return all moves when king takes an opposite piece`() {
                // given
                val game = givenAChessGame(
                    fenPiecePlacementOnly = "8/8/8/4p3/3K4/8/8/8",
                    isBlackKingCastlePossible = false,
                    isBlackQueenCastlePossible = false,
                    isWhiteQueenCastlePossible = false,
                    isWhiteKingCastlePossible = false,
                )
                // when
                val legalMoves = Position(D4, WHITE_KING).getAllPseudoLegalMoves(game)
                // then
                expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(
                    C5, D5, E5, C4, E4, C3, D3, E3
                )
                expectThat(
                    legalMoves.contains(
                        Move(
                            piece = WHITE_KING,
                            from = D4,
                            destination = E5,
                            capturedPiece = BLACK_PAWN,
                        )
                    )
                ).isTrue()
            }

            @Test
            fun `should return all moves when there is a piece from same color around`() {
                // given
                val game = givenAChessGame(
                    fenPiecePlacementOnly = "8/8/8/8/8/8/P7/K7",
                    isBlackKingCastlePossible = false,
                    isBlackQueenCastlePossible = false,
                    isWhiteQueenCastlePossible = false,
                    isWhiteKingCastlePossible = false,
                )
                // when
                val legalMoves = Position(A1, WHITE_KING).getAllPseudoLegalMoves(game)
                // then
                expectThat(legalMoves.map { it.destination }).containsExactlyInAnyOrder(
                    B1, B2
                )
            }
        }
    }
}
