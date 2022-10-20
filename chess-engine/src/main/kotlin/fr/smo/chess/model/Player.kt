package fr.smo.chess.model

interface Player {
    fun nextPlay(gameState: GameState) : MoveRequest
    fun registerColor(colorForTheGame : Color)
}