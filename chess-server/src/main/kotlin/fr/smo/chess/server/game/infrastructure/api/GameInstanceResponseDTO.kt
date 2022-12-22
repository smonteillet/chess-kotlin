package fr.smo.chess.server.game.infrastructure.api

import fr.smo.chess.core.Position
import fr.smo.chess.core.Square
import fr.smo.chess.server.game.domain.GameId
import fr.smo.chess.server.game.domain.PlayerId

data class GameInstanceResponseDTO(
    val gameId: GameId,
    val renderedGame : String,
    val playerIdToMove: PlayerId,
    // FIXME board : to implement later. This will be useful when it will be no longer acceptable to have an
    // emoji renderered chessboard on client side. Client side will draw its own chessboard based on this model.
    // BTW, not a good idea here to use class Position from our core domain. We'll see when this is going to be
    // implemented
    val board : List<Position> = listOf(),
    // FIXME possibleMoveForNextPlayer : to implement later. This will ensure that legal move calculation is
    // done only on server side and not duplicated on client side
    val possibleMoveForNextPlayer : Map<Square, List<Square>> = mapOf(),
)