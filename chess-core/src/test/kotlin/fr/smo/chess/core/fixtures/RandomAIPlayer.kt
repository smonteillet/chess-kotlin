package fr.smo.chess.core.fixtures

import fr.smo.chess.core.Color
import fr.smo.chess.core.Move
import fr.smo.chess.core.MoveCommand
import fr.smo.chess.core.Position
import fr.smo.chess.core.algs.getFirstPseudoLegalMoveSatisfying
import fr.smo.chess.core.utils.Failure
import fr.smo.chess.core.utils.Success
import fr.smo.chess.core.utils.ifTrue
import kotlin.random.Random

class RandomAIPlayer(seed: Long = System.currentTimeMillis()) : Player {

    private var color: Color? = null
    private val random = Random(seed)

    init {
        println("RandomAIPlayer seed: $seed")
    }

    override fun nextPlay(position: Position): MoveCommand {
        var move: Move? = null
        getFirstPseudoLegalMoveSatisfying(color!!, position, random) { candidateMove ->
            val legalMove = isMoveLegal(candidateMove, position)
            legalMove.ifTrue { move = candidateMove }
            legalMove && random.nextBoolean()
        }
        return move?.let {
            MoveCommand(origin = it.origin, destination = it.destination, promotedPiece = it.promotedTo?.type)
        }
            ?: throw IllegalStateException("This should not happen because game instance shall have stated that it is a stalemate on previous move")
    }

    private fun isMoveLegal(move: Move, position: Position): Boolean {
        return position.applyMove(
            MoveCommand(
                origin = move.origin,
                destination = move.destination,
                promotedPiece = move.promotedTo?.type,
            )
        ).let { outcome ->
            when (outcome) {
                is Success -> true
                is Failure -> false
            }
        }
    }

    override fun registerColor(colorForTheGame: Color) {
        color = colorForTheGame
    }
}