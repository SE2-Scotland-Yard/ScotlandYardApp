package at.aau.serg.websocketbrokerdemo.data.model

data class LeaveGameRequest(
    val gameId: String,
    val playerId: String
)
