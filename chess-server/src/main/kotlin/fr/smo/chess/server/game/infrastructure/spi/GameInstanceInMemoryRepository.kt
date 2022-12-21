package fr.smo.chess.server.game.infrastructure.spi

import fr.smo.chess.server.game.domain.GameInstance
import fr.smo.chess.server.game.domain.GameId
import fr.smo.chess.server.game.domain.GameInstanceRepository
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("singleton")
class GameInstanceInMemoryRepository : GameInstanceRepository {

    private val gameInstanceCache = mutableMapOf<GameId, GameInstance>()


    override fun save(gameInstance: GameInstance) : GameInstance {
        gameInstanceCache[gameInstance.gameId] = gameInstance
        return gameInstance
    }

    override fun findById(gameId: GameId): GameInstance {
        return gameInstanceCache[gameId]!!
    }

    override fun findOnlyChessGame() : GameInstance {
        return gameInstanceCache.values.first()
    }
}