package at.aau.serg.websocketbrokerdemo.viewmodel

import LobbyRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.websocketbrokerdemo.data.model.LobbyState
import at.aau.serg.websocketbrokerdemo.websocket.StompManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
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
    suspend fun tryJoinLobby(gameId: String, playerName: String): Result<String> {
        return repository.joinLobby(gameId, playerName)
            .onSuccess {
                fetchLobbyStatus(gameId)
                connectToLobby(gameId) { }
            }
    }


    /**
     * 4) WebSocket + STOMP verbinden und Updates einsammeln
     */
    fun connectToLobby(
        gameId: String,
        onConnected: () -> Unit = {}
    ) {
        stompManager.connectToLobby(
            gameId,
            onConnected = onConnected,
            onUpdate    = { lobby -> _lobbyStatus.value = lobby }
        )
        // zusätzlicher Collector, falls gewünscht
        viewModelScope.launch {
            stompManager.lobbyUpdates.collectLatest { _lobbyStatus.value = it }
        }
    }

    fun selectRole(gameId: String, player: String, role: String) {
        stompManager.sendSelectedRole(gameId, player, role)
    }


    fun leaveLobby(
        gameId: String,
        playerName: String,
        onLeft: () -> Unit
    ) = viewModelScope.launch {
        val result = withContext(Dispatchers.IO) {
            runCatching { repository.leaveLobby(gameId, playerName) }
        }

        result.onSuccess {
            stompManager.disconnect()
            _createdLobby.value = null

            /**  UI‑Callback muss auf Main!   */
            withContext(Dispatchers.Main) {
                onLeft()
            }
        }.onFailure { it.printStackTrace() }
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
}
