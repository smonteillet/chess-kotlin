package fr.smo.chess.server.game.domain

interface GameInstanceRepository {

    fun save(gameInstance: GameInstance) : GameInstance
    fun findById(gameId: GameId): GameInstance
    fun findOnlyChessGame() : GameInstance
    fun remove(updatedGame: GameInstance): GameInstance
}