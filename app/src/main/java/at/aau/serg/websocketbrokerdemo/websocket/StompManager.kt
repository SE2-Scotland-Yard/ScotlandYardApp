package at.aau.serg.websocketbrokerdemo.websocket

import android.content.Context
import android.util.Log
import at.aau.serg.websocketbrokerdemo.data.model.GameUpdate
import at.aau.serg.websocketbrokerdemo.data.model.LobbyState
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e("STOMP", "Unhandled coroutine exception: ${throwable.message}", throwable)
        }

        sessionJob = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            try {
                stompSession = client.connect(WS_URL)
                Log.d("STOMP", "WebSocket connected to $WS_URL")

                withContext(Dispatchers.Main) { onConnected() }

                launch {
                    stompSession?.subscribeText("/topic/lobby/$gameId")?.collect { message ->
                        Log.d("STOMP", "Lobby-Message: $message")
                        val lobby = Gson().fromJson(message, LobbyState::class.java)
                        _lobbyUpdates.value = lobby
                        onLobbyUpdate(lobby)
                    }
                }

                launch {
                    stompSession?.subscribeText("/topic/game/$gameId")?.collect { message ->
                        Log.d("STOMP", "Game-Message: $message")
                        val update = Gson().fromJson(message, GameUpdate::class.java)
                        _gameUpdates.value = update
                    }
                }

                launch {
                    stompSession?.subscribeText("/topic/game/$gameId/system")?.collect { message ->
                        Log.d("STOMP", "System-Message: $message")
                        withContext(Dispatchers.Main) {
                            onSystemMessage(message)
                            systemMessageHandler?.invoke(message)
                        }
                    }
                }

                launch {
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

    private suspend fun safeSendText(destination: String, payload: String) {
        try {
            val session = stompSession
            if (session == null) {
                Log.w("STOMP", "No active session. Cannot send to $destination")
                return
            }
            session.sendText(destination, payload)
            Log.d("STOMP", "Sent to $destination: $payload")
        } catch (e: Exception) {
            Log.e("STOMP", "Failed to send to $destination: ${e.message}", e)
        }
    }

    fun sendReady(gameId: String, playerName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val payload = Gson().toJson(mapOf("gameId" to gameId, "playerId" to playerName))
            safeSendText("/app/lobby/ready", payload)
        }
    }

    fun sendSelectedRole(gameId: String, player: String, role: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val payload = Gson().toJson(mapOf("gameId" to gameId, "playerId" to player, "role" to role))
            safeSendText("/app/lobby/role", payload)
        }
    }

    fun sendSelectedAvatar(gameId: String, player: String, avatarResId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val payload = Gson().toJson(mapOf("gameId" to gameId, "playerId" to player, "avatarResId" to avatarResId))
            safeSendText("/app/lobby/avatar", payload)
        }
    }

    fun sendLeaveLobby(gameId: String, playerId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val payload = Gson().toJson(mapOf("gameId" to gameId, "playerId" to playerId))
            safeSendText("/app/lobby/leave", payload)
        }
    }

    fun sendLobbyPing(gameId: String, playerId: String) {
        sendPingInternal("/app/lobby/ping", gameId, playerId)
    }

    fun sendGamePing(gameId: String, playerId: String) {
        sendPingInternal("/app/game/ping", gameId, playerId)
    }

    private fun sendPingInternal(destination: String, gameId: String, playerId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val payload = Gson().toJson(mapOf("gameId" to gameId, "playerId" to playerId))
            safeSendText(destination, payload)
        }
    }



    fun sendAddBot(gameId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val payload = Gson().toJson(mapOf("gameId" to gameId))
            safeSendText("/app/lobby/add-bot", payload)
        }
    }

    fun sendRemoveBot(gameId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val payload = Gson().toJson(mapOf("gameId" to gameId))
            safeSendText("/app/lobby/remove-bot", payload)
        }
    }


    fun disconnect() {
        sessionJob?.cancel()
        sessionJob = null
        stompSession = null
        Log.d("STOMP", "Disconnected")
    }
}
