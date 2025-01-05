package fr.smo.chess.core

import fr.smo.chess.core.notation.FEN.importFEN
import fr.smo.chess.core.notation.STARTING_STANDARD_POSITION_FEN
import fr.smo.chess.core.variant.Chess960
import fr.smo.chess.core.variant.Standard
import fr.smo.chess.core.variant.Variant

object GameFactory {

    fun createStandardGame() = importFEN(STARTING_STANDARD_POSITION_FEN, Standard)

    fun createGame(variant: Variant) : Position = importFEN(variant.getStartingFenPosition(), variant)

    fun create960Game(startingPosition: String?): Position {
        val chess960StartingPosition = startingPosition ?: Chess960.allStartingPositions.random()
        if (Chess960.allStartingPositions.contains(chess960StartingPosition).not()) {
            throw IllegalArgumentException("$startingPosition does not exist for Chess 960")
        }
        return importFEN(Chess960.startingPositionToFullFen(chess960StartingPosition), Chess960)
    }

}