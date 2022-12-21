package fr.smo.chess.server.game.domain

class PlayerId(
    val value : String
) {
    companion object {
        fun create() = GameId(System.currentTimeMillis().toString())
        fun of(value : String) = PlayerId(value)
    }
}