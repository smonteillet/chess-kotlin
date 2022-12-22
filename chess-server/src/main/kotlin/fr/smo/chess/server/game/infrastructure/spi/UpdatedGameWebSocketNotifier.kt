package fr.smo.chess.server.game.infrastructure.spi

import fr.smo.chess.core.renderer.GameRenderer
import fr.smo.chess.server.game.domain.GameInstance
import fr.smo.chess.server.game.domain.UpdatedGameNotifier
import fr.smo.chess.server.game.infrastructure.api.GameInstanceResponseDTO
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

@Component
class UpdatedGameWebSocketNotifier(
    private val simpMessagingTemplate: SimpMessagingTemplate
) : UpdatedGameNotifier {

    override fun sendUpdatedGameToPlayers(gameInstance: GameInstance) {
        println("Broadcasting new game state for ${gameInstance.gameId}")
        simpMessagingTemplate.convertAndSend(
            "/update/${gameInstance.gameId.value}",
            GameInstanceResponseDTO(
                gameId = gameInstance.gameId,
                renderedGame = GameRenderer.consoleRender(gameInstance.game),
                playerIdToMove = gameInstance.players[gameInstance.game.sideToMove]!!
            )
        )
    }

}