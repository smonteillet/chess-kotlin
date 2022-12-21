package fr.smo.chess.server.game.domain

import fr.smo.chess.core.Color
import fr.smo.chess.core.Game
import fr.smo.chess.core.MoveRequest
import java.util.*

data class GameInstance(
    val gameId: GameId,
    val players: Map<Color, PlayerId> = mapOf(),
    val game: Game,
    val callbackAfterMove: (Game) -> Unit = {},
) {


    fun registerNewPlayer(id: PlayerId): GameInstance {
        val color = if (players.size == 1) {
            players.keys.iterator().next().opposite()
        } else if (players.isEmpty()) {
            if (Random().nextInt() % 2 == 0) Color.WHITE else Color.BLACK
        } else {
            throw IllegalStateException("Could not register more than 2 players on a game")
        }
        val newPlayers = players.toMutableMap()
        newPlayers[color] = id
        return this.copy(
            players = newPlayers
        )
    }

    fun applyMove(moveRequest: MoveRequest): GameInstance {
        return this.copy(
            game = game.applyMove(moveRequest, callbackAfterMove)
        )
    }

}