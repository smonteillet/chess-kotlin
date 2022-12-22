package fr.smo.chess.server.game.infrastructure.api

import fr.smo.chess.core.Color
import fr.smo.chess.core.MoveRequest
import fr.smo.chess.core.Square
import fr.smo.chess.server.game.domain.GameId
import fr.smo.chess.server.game.domain.PlayerId
import fr.smo.chess.core.renderer.GameRenderer
import fr.smo.chess.server.game.domain.GameInstanceService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/game")
class GameResource(
    private val gameInstanceService: GameInstanceService,
    private val simpMessagingTemplate : SimpMessagingTemplate,
) {
    @PostMapping("/create/{playerId}")
    fun createGame(@PathVariable playerId: String): GameId {
       return gameInstanceService
            .createStandardChessGame()
            .let { gameInstanceService.registerNewPlayer(it.gameId, PlayerId.of(playerId)) }
            .gameId
    }

    @PostMapping("/join/{gameId}/{playerId}")
    @SendTo("/move/receive")
    fun joinGame(@PathVariable gameId: GameId, @PathVariable playerId: String) {
        gameInstanceService.registerNewPlayer(gameId, PlayerId.of(playerId))
        val chessGame = gameInstanceService.startGame(gameId)
        simpMessagingTemplate.convertAndSend( // fixme this should be handle in a clean architecture way inside the applyMove method
            "/move/receive",
            GameInstanceResponseDTO(
                gameId = gameId,
                renderedGame = GameRenderer.consoleRender(chessGame.game),
                playerIdToMove = chessGame.players[Color.WHITE]!!
            )
        )
    }

    @MessageMapping("/send")
    @SendTo("/move/receive")
    fun sendMove(@Payload request: GameInstanceRequestDTO): GameInstanceResponseDTO
 {
        return gameInstanceService.applyMove(
            gameId = request.gameId,
            moveRequest = MoveRequest(
                from = Square.Companion.at(request.moveFrom),
                destination = Square.Companion.at(request.moveDestination),
            )
        ).let { chessGame ->
            GameInstanceResponseDTO(
                gameId = chessGame.gameId,
                renderedGame = GameRenderer.consoleRender(chessGame.game),
                playerIdToMove = chessGame.players[chessGame.game.sideToMove]!!
            )
        }
    }
}