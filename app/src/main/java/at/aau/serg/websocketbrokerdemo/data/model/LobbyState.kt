package at.aau.serg.websocketbrokerdemo.data.model

import com.google.gson.annotations.SerializedName

data class LobbyState(
    val gameId: String,
    val players: List<String>,
    val readyStatus: Map<String, Boolean>,
    @SerializedName("public") val isPublic: Boolean,
    @SerializedName("started") val isStarted: Boolean,
    val maxPlayers: Int,
    val currentPlayerCount: Int,
    val selectedRoles: Map<String, String> = emptyMap(),
    val position: Int
)

