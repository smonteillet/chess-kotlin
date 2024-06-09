package fr.smo.chess.core.debug

import fr.smo.chess.core.*
import fr.smo.chess.core.algs.getAllPseudoLegalMovesForColor
import fr.smo.chess.core.utils.Failure
import fr.smo.chess.core.utils.Success
import fr.smo.chess.core.utils.ifTrue
import java.util.concurrent.atomic.AtomicLong

// https://www.chessprogramming.org/Perft_Results#cite_note-1

data class PerftResult(
        val nodesCount: Long = 0,
        val captures: Long = 0,
        val checkMates: Long = 0,
        val castles: Long = 0,
        val checks: Long = 0,
)

class Perft {

    private val captures = AtomicLong(0)
    private val nodesCount = AtomicLong(0)
    private val checkMates = AtomicLong(0)
    private val castles = AtomicLong(0)
    private val checks = AtomicLong(0)
    private val enPassant = AtomicLong(0)

    fun perft(game: Game, depth: Int): PerftResult {
        captures.set(0)
        nodesCount.set(0)
        checkMates.set(0)
        castles.set(0)
        checks.set(0)
        enPassant.set(0)
        perftRec(game, depth)
        return PerftResult(
                nodesCount = nodesCount.get(),
                captures = captures.get(),
                checkMates = checkMates.get(),
                castles = castles.get(),
                checks = checks.get(),
        )
    }

    private fun perftRec(game: Game, depth: Int) {
        if (depth == 0) {
            incrementCounters(game)
            return
        }
        return getAllPseudoLegalMovesForColor(game.sideToMove, game)
                .map { MoveCommand(origin = it.origin, destination = it.destination, promotedPiece = it.promotedTo?.type) }
                .forEach { moveCommand ->
                    when (val moveOutcome = game.applyMove(moveCommand)) {
                        is Success -> perftRec(moveOutcome.value, depth - 1)
                        is Failure -> {}
                    }
                }
    }

    private fun incrementCounters(game: Game) {
        nodesCount.incrementAndGet()
        if (game.history.moves.last().capturedPiece != null) {
            captures.incrementAndGet()
        }
        (game.enPassantTargetSquare != null).ifTrue { enPassant.incrementAndGet() }
        (game.history.moves.last().isCheck).ifTrue {
            checks.incrementAndGet()
        }
        (game.history.moves.last().isKingCastle || game.history.moves.last().isQueenCastle).ifTrue {
            castles.incrementAndGet()
        }
        if (game.status == Status.BLACK_WIN || game.status == Status.WHITE_WIN) {
            checkMates.incrementAndGet()
        }
    }

}

