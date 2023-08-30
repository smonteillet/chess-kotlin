package fr.smo.chess.server.game.domain

import fr.smo.chess.core.Game
import fr.smo.chess.core.GameFactory
import fr.smo.chess.core.MoveCommand
import fr.smo.chess.core.renderer.GameRenderer

class GameInstanceService(
    private val gameInstanceRepository: GameInstanceRepository,
    private val updatedGameNotifier: UpdatedGameNotifier,
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
            ).also {
                gameInstanceRepository.save(it)
                updatedGameNotifier.sendUpdatedGameToPlayers(it)
            }
        }

    fun applyMove(gameId : GameId, moveCommand: MoveCommand): GameInstance =
        gameInstanceRepository
            .findById(gameId)
            .applyMove(moveCommand)
            .also { updatedGame ->
                updatedGameNotifier.sendUpdatedGameToPlayers(updatedGame)
                if (updatedGame.game.gameIsOver)
                    gameInstanceRepository.remove(updatedGame)
                else
                    gameInstanceRepository.save(updatedGame)
            }
}