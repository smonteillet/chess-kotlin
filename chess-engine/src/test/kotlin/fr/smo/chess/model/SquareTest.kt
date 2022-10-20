package fr.smo.chess.model

import fr.smo.chess.model.Square.Companion.square
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

internal class SquareTest {

    @Test
    fun up() {
        expectThat(square("d4").up()) isEqualTo  square("d5")
    }

    @Test
    fun upLeft() {
        expectThat(square("d4").upLeft()) isEqualTo  square("c5")
    }

    @Test
    fun upRight() {
        expectThat(square("d4").upRight()) isEqualTo  square("e5")
    }


    @Test
    fun down() {
        expectThat(square("d4").down()) isEqualTo square("d3")
    }

    @Test
    fun downLeft() {
        expectThat(square("d4").downLeft()) isEqualTo square("c3")
    }

    @Test
    fun downRight() {
        expectThat(square("d4").downRight()) isEqualTo square("e3")
    }

    @Test
    fun left() {
        expectThat(square("d4").left()) isEqualTo square("c4")
    }

    @Test
    fun right() {
        expectThat(square("d4").right()) isEqualTo square("e4")
    }

    @Test
    fun outOfBoundFromTop() {
        expectThat(square("h8").up()).isNull()
    }

    @Test
    fun outOfBoundFromBottom() {
        expectThat(square("a1").down()).isNull()
    }

    @Test
    fun outOfBoundFromLeft() {
        expectThat(square("a1").left()).isNull()
    }

    @Test
    fun outOfBoundFromRight() {
        expectThat(square("h8").right()).isNull()
    }

    @Test
    fun testEquality() {
        expectThat(square("d8")) isEqualTo square("d8")
    }

    @Test
    fun testToTheVeryLeft() {
        expectThat(square("h7").toTheVeryLeft()) isEqualTo square("a7")
    }
    @Test
    fun testWithRow() {
        expectThat(square("h7").withRow(3)) isEqualTo square("h3")

    }

}