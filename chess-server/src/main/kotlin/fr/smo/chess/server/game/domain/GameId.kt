package fr.smo.chess.server.game.domain

data class GameId(
    val value : String
) {
    companion object {
        fun create() = GameId(System.currentTimeMillis().toString())
    }
}