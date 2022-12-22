package fr.smo.chess.server.game.infrastructure.config

import fr.smo.chess.server.game.domain.GameInstanceRepository
import fr.smo.chess.server.game.domain.GameInstanceService
import fr.smo.chess.server.game.domain.UpdatedGameNotifier
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class BeanConfiguration {

    @Bean
    open fun createGameInstanceService(
        @Autowired gameInstanceRepository: GameInstanceRepository,
        @Autowired updatedGameNotifier: UpdatedGameNotifier,
    ) : GameInstanceService {
        return GameInstanceService(gameInstanceRepository, updatedGameNotifier)
    }

}
