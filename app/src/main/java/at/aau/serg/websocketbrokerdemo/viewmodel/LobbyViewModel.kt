package at.aau.serg.websocketbrokerdemo.viewmodel

import LobbyRepository
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.websocketbrokerdemo.data.model.GameUpdate
import at.aau.serg.websocketbrokerdemo.data.model.LobbyState
import at.aau.serg.websocketbrokerdemo.websocket.StompManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LobbyViewModel(
    private val repository: LobbyRepository = LobbyRepository()
) : ViewModel() {

    private val _createdLobby = MutableStateFlow<LobbyState?>(null)
    val createdLobby = _createdLobby.asStateFlow()

    private val _lobbyStatus = MutableStateFlow<LobbyState?>(null)
    val lobbyStatus = _lobbyStatus.asStateFlow()

    private val _publicLobbies = MutableStateFlow<List<LobbyState>>(emptyList())
    val publicLobbies = _publicLobbies.asStateFlow()

    private val stompManager = StompManager()

    private val _gameState = MutableStateFlow<GameUpdate?>(null)
    val gameState = _gameState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage


    var alreadyConnected = false

    /** 1) neue Lobby erstellen */
    fun createLobby(isPublic: Boolean, creatorName: String) = viewModelScope.launch {
        runCatching {
            repository.createLobby(isPublic, creatorName)
        }.onSuccess { _createdLobby.value = it }
            .onFailure { it.printStackTrace() }
    }

    /** 2) Lobby‑Status per REST holen */
    fun fetchLobbyStatus(gameId: String) = viewModelScope.launch {
        runCatching {
            repository.getLobbyStatus(gameId)
        }.onSuccess { _lobbyStatus.value = it }
            .onFailure { _lobbyStatus.value = null }
    }

    /**
     * 3) Lobby per REST beitreten → danach WS aufbauen
     */
    suspend fun tryJoinLobby(gameId: String, playerName: String): String {
        return runCatching {
            println("Versuche Lobby beizutreten: gameId=$gameId, playerName=$playerName")

            val joinResponse = repository.joinLobby(gameId, playerName)
            println("Beitritt erfolgreich, Antwort: ${joinResponse.message}")

            fetchLobbyStatus(gameId)
            connectToLobby(gameId)

            joinResponse.message
        }.getOrElse {
            println("Fehler beim Beitritt: ${it.message}")
            it.printStackTrace()
            ""
        }
    }


    /**
     * 4) WebSocket + STOMP verbinden und Updates einsammeln
     */
    fun connectToLobby(
        gameId: String,
        onConnected: () -> Unit = {}
    ) {
        if (alreadyConnected) return

        stompManager.connectToAllTopics(
            gameId,
            onConnected = onConnected,
            onLobbyUpdate = { _lobbyStatus.value = it },
            onErrorMessage = { msg -> _errorMessage.value = msg }

        )


        viewModelScope.launch {
            stompManager.lobbyUpdates.collectLatest { _lobbyStatus.value = it }
        }

        viewModelScope.launch {
            stompManager.gameUpdates.collectLatest { _gameState.value = it }
        }

        alreadyConnected = true
    }





    fun selectRole(gameId: String, player: String, role: String) {
        stompManager.sendSelectedRole(gameId, player, role)
    }


    fun sendLeave(gameId: String, player: String, onLeft: () -> Unit) = viewModelScope.launch {
        stompManager.sendLeaveLobby(gameId, player)
        delay(200)

        stompManager.disconnect()
        alreadyConnected = false

        _createdLobby.value = null
        _lobbyStatus.value = null

        withContext(Dispatchers.Main) {
            onLeft()
        }
    }




    /** 5) Ready senden */
    fun sendReady(gameId: String, playerName: String) {
        stompManager.sendReady(gameId, playerName)
    }

    /** 6) öffentliche Lobbys */
    fun fetchPublicLobbies() = viewModelScope.launch {
        runCatching {
            repository.getPublicLobbies()
        }.onSuccess { _publicLobbies.value = it }
            .onFailure { _publicLobbies.value = emptyList() }
    }

    /** 7) Aufräumen */
    override fun onCleared() {
        stompManager.disconnect()
        super.onCleared()
    }

    fun selectAvatar(gameId: String, player: String, avatarResId: Int) {
        stompManager.sendSelectedAvatar(gameId, player, avatarResId)
    }

    fun clearError() {
        _errorMessage.value = null
    }



}
