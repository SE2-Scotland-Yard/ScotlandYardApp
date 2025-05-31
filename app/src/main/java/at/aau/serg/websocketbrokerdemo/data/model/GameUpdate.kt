package at.aau.serg.websocketbrokerdemo.data.model



data class GameUpdate(
    val gameId: String,
    val playerPositions: Map<String, Int>,
    val currentPlayer: String,
    val winner:String,
    val lastTicketUsed: String,
    val ticketInventory: Map<String, Map<String, Int>>
)