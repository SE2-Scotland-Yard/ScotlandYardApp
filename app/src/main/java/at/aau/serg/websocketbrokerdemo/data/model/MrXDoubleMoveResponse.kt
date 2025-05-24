package at.aau.serg.websocketbrokerdemo.data.model

data class MrXDoubleMoveResponse(
    val firstTo: Int,
    val firstTicket: String,
    val secondTo: Int,
    val secondTicket: String
)