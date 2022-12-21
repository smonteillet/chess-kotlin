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
            .createChessGame()
            .let { gameInstanceService.registerNewPlayer(it.gameId, PlayerId.of(playerId)) }
            .gameId
    }

    @PostMapping("/join/{gameId}/{playerId}")
    @SendTo("/move/receive")
    fun joinGame(@PathVariable gameId: GameId, @PathVariable playerId: String) {
        gameInstanceService.registerNewPlayer(gameId, PlayerId.of(playerId))
        val chessGame = gameInstanceService.startGame(gameId)
        simpMessagingTemplate.convertAndSend(
            "/move/receive",
            ChessGameWebSocketResponse(
                renderedChessboard = GameRenderer.consoleRender(chessGame.game),
                playerIdToPlay = chessGame.players[Color.WHITE]!!
            )
        )
    }

    @MessageMapping("/send")
    @SendTo("/move/receive")
    fun sendMove(@Payload request: ChessGameWebSocketRequest): ChessGameWebSocketResponse {
        return gameInstanceService.applyMove(
            MoveRequest(
                from = Square.Companion.at(request.from),
                destination = Square.Companion.at(request.destination),
            )
        ).let { chessGame -> // fixme this should be handle in a clean architecture way inside the applyMove method
            ChessGameWebSocketResponse(
                renderedChessboard = GameRenderer.consoleRender(chessGame.game),
                playerIdToPlay = chessGame.players[chessGame.game.sideToMove]!!
            )
        }
    }

    data class ChessGameWebSocketRequest(val from: String, val destination: String)
    data class ChessGameWebSocketResponse(
        val renderedChessboard: String,
        val playerIdToPlay : PlayerId,
    )

}