package at.aau.serg.websocketbrokerdemo.websocket

import android.util.Log
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
        onConnected: () -> Unit = {},
        onLobbyUpdate: (LobbyState) -> Unit = {}
    ) {
        sessionJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                stompSession = client.connect(WS_URL)
                Log.d("STOMP", "WebSocket connected to $WS_URL")

                withContext(Dispatchers.Main) { onConnected() }

                // Lobby-Subscription in eigener Coroutine
                launch {
                    Log.d("STOMP", "Subscribing to /topic/lobby/$gameId")
                    stompSession?.subscribeText("/topic/lobby/$gameId")?.collect { message ->
                        Log.d("STOMP", "Lobby-Message: $message")
                        val lobby = Gson().fromJson(message, LobbyState::class.java)
                        _lobbyUpdates.value = lobby
                        onLobbyUpdate(lobby)
                    }
                }

                // Game-Subscription in eigener Coroutine
                launch {
                    Log.d("STOMP", "Subscribing to /topic/game/$gameId")
                    stompSession?.subscribeText("/topic/game/$gameId")?.collect { message ->
                        Log.d("STOMP", " Game-Message: $message")
                        val update = Gson().fromJson(message, GameUpdate::class.java)
                        _gameUpdates.value = update
                    }
                }

            } catch (e: Exception) {
                Log.e("STOMP", "Connection or subscription failed: ${e.message}", e)
            }
        }
    }




    fun connectToGame(gameId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("STOMP", "Attempting to subscribe to /topic/game/$gameId")

                stompSession
                    ?.subscribeText("/topic/game/$gameId")
                    ?.collectLatest { message ->
                        Log.d("STOMP", "Game-Message received: $message")
                        val gameUpdate = Gson().fromJson(message, GameUpdate::class.java)
                        _gameUpdates.value = gameUpdate
                    }

                Log.d("STOMP", "Subscribed to /topic/game/$gameId")
            } catch (e: Exception) {
                Log.e("STOMP", "ame subscription failed: ${e.message}", e)
            }
        }
    }


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

    fun disconnect() {
        sessionJob?.cancel()
        stompSession = null
        Log.d("STOMP", "Disconnected")
    }
}
