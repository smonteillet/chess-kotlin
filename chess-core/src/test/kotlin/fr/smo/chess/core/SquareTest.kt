package fr.smo.chess.core


import fr.smo.chess.core.Square.*
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEqualTo
import strikt.assertions.isNull

internal class SquareTest {

    @Test
    fun up() {
        expectThat(D4.up()) isEqualTo D5
    }

    @Test
    fun upLeft() {
        expectThat(D4.upLeft()) isEqualTo C5
    }

    @Test
    fun upRight() {
        expectThat(D4.upRight()) isEqualTo E5
    }


    @Test
    fun down() {
        expectThat(D4.down()) isEqualTo D3
    }

    @Test
    fun downLeft() {
        expectThat(D4.downLeft()) isEqualTo C3
    }

    @Test
    fun downRight() {
        expectThat(D4.downRight()) isEqualTo E3
    }

    @Test
    fun left() {
        expectThat(D4.left()) isEqualTo C4
    }

    @Test
    fun right() {
        expectThat(D4.right()) isEqualTo E4
    }

    @Test
    fun outOfBoundFromTop() {
        expectThat(H8.up()).isNull()
    }

    @Test
    fun outOfBoundFromBottom() {
        expectThat(A1.down()).isNull()
    }

    @Test
    fun outOfBoundFromLeft() {
        expectThat(A1.left()).isNull()
    }

    @Test
    fun outOfBoundFromRight() {
        expectThat(H8.right()).isNull()
    }

    @Test
    fun testEquality() {
        expectThat(D8) isEqualTo D8
    }

    @Test
    fun testToTheVeryLeft() {
        expectThat(H7.toTheVeryLeft()) isEqualTo A7
    }

    @Test
    fun testWithRow() {
        expectThat(H7.withRank(Rank.RANK_3)) isEqualTo H3
    }

    @Test
    fun `should test weird case`() {
        expectThat(B1) isNotEqualTo A2

    }

}