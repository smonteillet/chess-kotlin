package fr.smo.chess.server.game.infrastructure.api

import fr.smo.chess.server.game.domain.GameId
import fr.smo.chess.server.game.domain.PlayerId

data class GameInstanceRequestDTO(
    val gameId: GameId,
    val playerId: PlayerId,
    val moveFrom: String,
    val moveDestination: String,
    val promotedTo: String?,
)