package fr.smo.chess.server.game.domain

import fr.smo.chess.core.Color
import fr.smo.chess.core.Game
import fr.smo.chess.core.MoveCommand
import java.util.*

data class GameInstance(
    val gameId: GameId,
    val players: Map<Color, PlayerId> = mapOf(),
    val game: Game,
    val callbackAfterMove: (Game) -> Unit = {},
) {

    fun registerNewPlayer(playerId: PlayerId): GameInstance {
        val assignedColor = when (players.size) {
            0 -> if (Random().nextInt() % 2 == 0) Color.WHITE else Color.BLACK
            1 -> players.keys.first().opposite()
            else -> throw IllegalStateException("Could not register more than 2 players on a game")
        }
        return this.copy(
            players = players.plus(assignedColor to playerId)
        )
    }

    fun applyMove(moveCommand: MoveCommand): GameInstance {
        return this.copy(
            game = game.applyMove(moveCommand)
        ).also { callbackAfterMove.invoke(it.game) }
    }

}