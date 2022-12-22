package fr.smo.chess.server.game.domain

import fr.smo.chess.core.Game
import fr.smo.chess.core.GameFactory
import fr.smo.chess.core.MoveRequest
import fr.smo.chess.core.renderer.GameRenderer
import org.springframework.stereotype.Component

//FIXME remove spring annotation
@Component
class GameInstanceService(
    private val gameInstanceRepository: GameInstanceRepository,
) {

    fun createStandardChessGame(): GameInstance =
        gameInstanceRepository.save(GameInstance(
            gameId = GameId.create(),
            game = GameFactory.createStandardGame(),
            callbackAfterMove = { println(GameRenderer.consoleRender(it)) }
        ))

    fun registerNewPlayer(gameId: GameId, playerId: PlayerId): GameInstance =
        gameInstanceRepository.findById(gameId).let { gameInstance ->
            gameInstance.registerNewPlayer(playerId)
                .also { gameInstanceRepository.save(it) }
        }

    fun startGame(gameId: GameId): GameInstance =
        gameInstanceRepository.findById(gameId).let { gameInstance ->
            gameInstance.copy(
                game = gameInstance.game.copy(status = Game.Status.IN_PROGRESS)
            ).also { gameInstanceRepository.save(it) }
        }

    fun applyMove(gameId : GameId, moveRequest: MoveRequest): GameInstance =
        gameInstanceRepository
            .findById(gameId)
            .applyMove(moveRequest)
            .also { updatedGame ->
                if (updatedGame.game.gameIsOver)
                    gameInstanceRepository.remove(updatedGame)
                else
                    gameInstanceRepository.save(updatedGame)

            }
}