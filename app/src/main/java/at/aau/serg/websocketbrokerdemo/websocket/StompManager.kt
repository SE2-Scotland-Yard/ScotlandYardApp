package at.aau.serg.websocketbrokerdemo.websocket

import android.content.Context
import android.util.Log
import android.widget.Toast
import at.aau.serg.websocketbrokerdemo.data.model.GameUpdate
import at.aau.serg.websocketbrokerdemo.data.model.LobbyState
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import org.json.JSONObject

class StompManager {

    companion object {
        private const val WS_URL = "ws://10.0.2.2:8080/ws-stomp"
    }

    private val client = StompClient(OkHttpWebSocketClient())
    private var sessionJob: Job? = null
    private var stompSession: StompSession? = null

    private val _lobbyUpdates = MutableStateFlow<LobbyState?>(null)
    val lobbyUpdates = _lobbyUpdates.asStateFlow()

    private val _gameUpdates = MutableStateFlow<GameUpdate?>(null)
    val gameUpdates = _gameUpdates.asStateFlow()

    fun connectToAllTopics(
        gameId: String,
        context: Context,
        onConnected: () -> Unit = {},
        onLobbyUpdate: (LobbyState) -> Unit = {},
        onErrorMessage: (String) -> Unit = {},
        onSystemMessage: (String) -> Unit = {}

    ) {
        sessionJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                stompSession = client.connect(WS_URL)
                Log.d("STOMP", "WebSocket connected to $WS_URL")

                withContext(Dispatchers.Main) { onConnected() }

                // LOBBY updates
                launch {
                    Log.d("STOMP", "Subscribing to /topic/lobby/$gameId")
                    stompSession?.subscribeText("/topic/lobby/$gameId")?.collect { message ->
                        Log.d("STOMP", "Lobby-Message: $message")
                        val lobby = Gson().fromJson(message, LobbyState::class.java)
                        _lobbyUpdates.value = lobby
                        onLobbyUpdate(lobby)
                    }
                }

                // GAME updates
                launch {
                    Log.d("STOMP", "Subscribing to /topic/game/$gameId")
                    stompSession?.subscribeText("/topic/game/$gameId")?.collect { message ->
                        Log.d("STOMP", "Game-Message: $message")
                        val update = Gson().fromJson(message, GameUpdate::class.java)
                        _gameUpdates.value = update
                    }
                }

                // SYSTEM-Nachrichten
                launch {
                    Log.d("STOMP", "Subscribing to /topic/game/$gameId/system")
                    stompSession?.subscribeText("/topic/game/$gameId/system")?.collect { message ->
                        Log.d("STOMP", "System-Message: $message")
                        withContext(Dispatchers.Main) {
                            onSystemMessage(message)
                            systemMessageHandler?.invoke(message)
                        }
                    }
                }



                // ERROR messages
                launch {
                    Log.d("STOMP", "Subscribing to /topic/lobby/$gameId/error")
                    stompSession?.subscribeText("/topic/lobby/$gameId/error")?.collect { message ->
                        Log.d("STOMP", "Error-Message: $message")
                        val json = JSONObject(message)
                        val errorText = json.optString("error", "Unbekannter Fehler")
                        withContext(Dispatchers.Main) {
                            onErrorMessage(errorText)
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e("STOMP", "Connection or subscription failed: ${e.message}", e)
            }
        }
    }

    var systemMessageHandler: ((String) -> Unit)? = null




    fun sendReady(gameId: String, playerName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val payload = Gson().toJson(
                    mapOf("gameId" to gameId, "playerId" to playerName)
                )
                stompSession?.sendText("/app/lobby/ready", payload)
                Log.d("STOMP", "Sent ready: $payload")
            } catch (e: Exception) {
                Log.e("STOMP", "Sending failed: ${e.message}", e)
            }
        }
    }

    fun sendSelectedRole(gameId: String, player: String, role: String) {
        val payload = Gson().toJson(
            mapOf("gameId" to gameId, "playerId" to player, "role" to role)
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                stompSession?.sendText("/app/lobby/role", payload)
            } catch (e: Exception) {
                Log.e("STOMP", "Role selection failed: ${e.message}", e)
            }
        }
    }

    fun sendSelectedAvatar(gameId: String, player: String, avatarResId: Int) {
        val payload = Gson().toJson(
            mapOf("gameId" to gameId, "playerId" to player, "avatarResId" to avatarResId)
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                stompSession?.sendText("/app/lobby/avatar", payload)
            } catch (e: Exception) {
                Log.e("STOMP", "Avatar selection failed: ${e.message}", e)
            }
        }
    }

    fun sendLeaveLobby(gameId: String, playerId: String) {
        val payload = mapOf("gameId" to gameId, "playerId" to playerId)
        CoroutineScope(Dispatchers.IO).launch {
            try {

                val json = Gson().toJson(payload)
                stompSession?.sendText("/app/lobby/leave", json)
                Log.d("STOMP", "Leave request sent: $json")
            } catch (e: Exception) {
                Log.e("STOMP", "Error sending leave request: ${e.message}", e)
            }
        }
    }
    //aktivität senden
    fun sendLobbyPing(gameId: String, playerId: String) {
        sendPingInternal("/app/lobby/ping", gameId, playerId)
    }

    fun sendGamePing(gameId: String, playerId: String) {
        sendPingInternal("/app/game/ping", gameId, playerId)
    }

    private fun sendPingInternal(destination: String, gameId: String, playerId: String) {
        val payload = mapOf("gameId" to gameId, "playerId" to playerId)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = Gson().toJson(payload)
                stompSession?.sendText(destination, json)
                Log.d("STOMP", "Ping sent to $destination: $json")
            } catch (e: Exception) {
                Log.e("STOMP", "Error sending ping to $destination: ${e.message}", e)
            }
        }
    }


    suspend fun sendLeaveGame(gameId: String, playerId: String) {
        val json = Gson().toJson(mapOf("gameId" to gameId, "playerId" to playerId))

        stompSession?.sendText("/app/game/leave", json)
    }


    fun disconnect() {
        sessionJob?.cancel()
        sessionJob = null
        stompSession = null
        Log.d("STOMP", "Disconnected")
    }

}
