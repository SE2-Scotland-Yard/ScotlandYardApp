package at.aau.serg.websocketbrokerdemo.data.model

import android.app.GameState

data class GameUpdate(
    val gameId: String,
    val playerPositions: Map<String, Int>,
    val currentPlayer: String,
    val winner:String,
    val lastTicketUsed: String

)