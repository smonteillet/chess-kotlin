package fr.smo.chess.server.game.infrastructure.api

import fr.smo.chess.core.MoveRequest
import fr.smo.chess.core.Square
import fr.smo.chess.server.game.domain.GameId
import fr.smo.chess.server.game.domain.GameInstance
import fr.smo.chess.server.game.domain.GameInstanceService
import fr.smo.chess.server.game.domain.PlayerId
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/game")
class GameResource(
    private val gameInstanceService: GameInstanceService,
) {
    @PostMapping("/create/{playerId}")
    fun createGame(@PathVariable playerId: String): GameId {
        return gameInstanceService
            .createStandardChessGame()
            .let { gameInstanceService.registerNewPlayer(it.gameId, PlayerId.of(playerId)) }
            .gameId
    }

    @PostMapping("/join/{gameId}/{playerId}")
    fun joinGame(@PathVariable gameId: GameId, @PathVariable playerId: PlayerId) : GameId {
        gameInstanceService.registerNewPlayer(gameId, playerId)
        return gameInstanceService.startGame(gameId).gameId
    }

    @MessageMapping("/send/{gameId}")
    fun sendMove(@DestinationVariable gameId : String, @Payload request: GameInstanceRequestDTO) : GameInstance {
        return gameInstanceService.applyMove(
            gameId = GameId(gameId),
            moveRequest = MoveRequest(
                from = Square.Companion.at(request.moveFrom),
                destination = Square.Companion.at(request.moveDestination),
            )
        )
    }
}