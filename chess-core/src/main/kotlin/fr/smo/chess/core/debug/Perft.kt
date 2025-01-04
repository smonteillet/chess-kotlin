package fr.smo.chess.core.debug

import fr.smo.chess.core.MoveCommand
import fr.smo.chess.core.Position
import fr.smo.chess.core.Status
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

    fun perft(position: Position, depth: Int): PerftResult {
        captures.set(0)
        nodesCount.set(0)
        checkMates.set(0)
        castles.set(0)
        checks.set(0)
        enPassant.set(0)
        perftRec(position, depth)
        return PerftResult(
            nodesCount = nodesCount.get(),
            captures = captures.get(),
            checkMates = checkMates.get(),
            castles = castles.get(),
            checks = checks.get(),
        )
    }

    private fun perftRec(position: Position, depth: Int) {
        if (depth == 0) {
            incrementCounters(position)
            return
        }
        return getAllPseudoLegalMovesForColor(position.sideToMove, position)
            .map { MoveCommand(origin = it.origin, destination = it.destination, promotedPiece = it.promotedTo?.type) }
            .forEach { moveCommand ->
                when (val moveOutcome = position.applyMove(moveCommand)) {
                    is Success -> perftRec(moveOutcome.value, depth - 1)
                    is Failure -> {}
                }
            }
    }

    private fun incrementCounters(position: Position) {
        nodesCount.incrementAndGet()
        if (position.history.moves.last().capturedPiece != null) {
            captures.incrementAndGet()
        }
        (position.enPassantTargetSquare != null).ifTrue { enPassant.incrementAndGet() }
        (position.history.moves.last().isCheck).ifTrue {
            checks.incrementAndGet()
        }
        (position.history.moves.last().isKingCastle || position.history.moves.last().isQueenCastle).ifTrue {
            castles.incrementAndGet()
        }
        if (position.status == Status.BLACK_WIN || position.status == Status.WHITE_WIN) {
            checkMates.incrementAndGet()
        }
    }

}

