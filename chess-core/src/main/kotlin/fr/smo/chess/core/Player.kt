package fr.smo.chess.core

interface Player {
    fun nextPlay(gameState: GameState) : MoveRequest
    fun registerColor(colorForTheGame : Color)
}