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

    fun createChessGame(): GameInstance =
        gameInstanceRepository.save(GameInstance(
            gameId = GameId.create(),
            game = GameFactory.createStandardGame(),
            callbackAfterMove = { println(GameRenderer.consoleRender(it)) }
        ))

    fun registerNewPlayer(gameId: GameId, playerId: PlayerId): GameInstance =
        gameInstanceRepository.findById(gameId).let { currentGameInstance ->
            currentGameInstance.registerNewPlayer(playerId)
                .also { gameInstanceRepository.save(it) }
        }

    fun startGame(gameId: GameId): GameInstance =
        gameInstanceRepository.findById(gameId).let { currentChessGame ->
            currentChessGame.copy(
                game = currentChessGame.game.copy(status = Game.Status.IN_PROGRESS)
            ).also { gameInstanceRepository.save(it) }
        }

    fun applyMove(moveRequest: MoveRequest): GameInstance =
        gameInstanceRepository
            .findOnlyChessGame() // FIXME, we shall have gameId to find GameInstance here
            .applyMove(moveRequest)
            .also { gameInstanceRepository.save(it) } //FIXME handle game over
}