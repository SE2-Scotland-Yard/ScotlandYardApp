package at.aau.serg.websocketbrokerdemo.ui.lobby

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import at.aau.serg.websocketbrokerdemo.viewmodel.LobbyViewModel
import at.aau.serg.websocketbrokerdemo.viewmodel.UserSessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    gameId: String,
    lobbyVm: LobbyViewModel,
    userSessionVm: UserSessionViewModel,
    onLeft: () -> Unit,
    onGameStarted: (String) -> Unit
) {
    /* ---------- State ---------- */
    val lobbyState by lobbyVm.lobbyStatus.collectAsState()
    val context      = LocalContext.current
    var toastShown  by remember { mutableStateOf(false) }

    LaunchedEffect(lobbyState?.isStarted) {
        if (lobbyState?.isStarted == true) {
            onGameStarted(gameId)
        }
    }


    /* ---------- REST + WebSocket einrichten ---------- */
    LaunchedEffect(gameId) {
        lobbyVm.fetchLobbyStatus(gameId)
        lobbyVm.connectToLobby(
            gameId,
            onConnected = {
                if (!toastShown) {
                    Toast.makeText(context, "WebSocket verbunden", Toast.LENGTH_SHORT).show()
                    toastShown = true
                }
            }
        )
    }
    /* ---------- wenn alle ready sein auf GameScreen wechseln ---------- */
    LaunchedEffect(lobbyState?.isStarted) {
        if (lobbyState?.isStarted == true) {
            onGameStarted(gameId)
        }
    }

    /* ---------- UI ---------- */
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lobby $gameId") },
                actions = {
                    TextButton(
                        onClick = {
                            val player = userSessionVm.username.value.orEmpty()
                            lobbyVm.leaveLobby(gameId, player) {
                                onLeft()
                            }
                        }
                    ) {
                        Text("Verlassen", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            lobbyState?.let { lobby ->
                Text("Spieler in der Lobby:", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                lobby.players.forEach { p ->
                    val ready = lobby.readyStatus[p] == true
                    Text("• $p ${if (ready) "(bereit)" else "(wartet)"}")
                }
                Spacer(Modifier.height(24.dp))
                if (!lobby.isStarted) {
                    Button(
                        onClick = {
                            val player = userSessionVm.username.value.orEmpty()
                            lobbyVm.sendReady(gameId, player)
                        }
                    ) { Text("Bereit") }
                } else {
                    Text("Das Spiel hat begonnen!", color = MaterialTheme.colorScheme.primary)
                }
            } ?: Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        }
    }
}
