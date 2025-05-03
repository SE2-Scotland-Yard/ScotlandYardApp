package at.aau.serg.websocketbrokerdemo.websocket

import android.util.Log
import at.aau.serg.websocketbrokerdemo.data.model.LobbyState
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient

class StompManager {

    companion object {

        private const val WS_URL = "ws://10.0.2.2:8080/ws-stomp"
    }

    private val client = StompClient(OkHttpWebSocketClient())
    private var sessionJob: Job? = null
    private var session    = client

    private val _lobbyUpdates = MutableStateFlow<LobbyState?>(null)
    val lobbyUpdates = _lobbyUpdates.asStateFlow()


    fun connectToLobby(
        gameId: String,
        onConnected: () -> Unit = {},
        onUpdate:    (LobbyState) -> Unit = {}
    ) {
        sessionJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val stompSession = client.connect(WS_URL)
                Log.d("STOMP", "WebSocket connected to $WS_URL")
                // Toasts/Compose-Callbacks nur auf Main
                withContext(Dispatchers.Main) {
                    onConnected()
                }
                // subscribe auf das Lobby-Topic
                stompSession
                    .subscribeText("/topic/lobby/$gameId")
                    .collect { message ->
                        Log.d("STOMP", "Message received: $message")
                        val lobby = Gson().fromJson(message, LobbyState::class.java)
                        _lobbyUpdates.value = lobby
                        onUpdate(lobby)
                    }
            } catch (e: Exception) {
                Log.e("STOMP", "Connection failed: ${e.message}", e)
            }
        }
    }

    /**
     * 4) sendReady() verwendet dieselbe Session zum Senden
     */
    fun sendReady(gameId: String, playerName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {

                val stompSession = client.connect(WS_URL)
                val payload = Gson().toJson(
                    mapOf("gameId" to gameId, "playerId" to playerName)
                )
                stompSession.sendText("/app/lobby/ready", payload)
                Log.d("STOMP", "Sent ready: $payload")
            } catch (e: Exception) {
                Log.e("STOMP", "Sending failed: ${e.message}", e)
            }
        }
    }

    fun sendSelectedRole(gameId: String, player: String, role: String) {
        val payload = Gson().toJson(mapOf(
            "gameId" to gameId,
            "playerId" to player,
            "role" to role
        ))
        CoroutineScope(Dispatchers.IO).launch {
            client.connect(WS_URL).sendText("/app/lobby/role", payload)
        }
    }


    /** 5) cancel + disconnect */
    fun disconnect() {
        sessionJob?.cancel()
        Log.d("STOMP", "Disconnected")
    }
}
