package fr.smo.chess.server.game.domain

interface UpdatedGameNotifier {

    fun sendUpdatedGameToPlayers(gameInstance : GameInstance)
}