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
    val lobbyState by lobbyVm.lobbyStatus.collectAsState()
    val context = LocalContext.current
    var toastShown by remember { mutableStateOf(false) }

    LaunchedEffect(lobbyState?.isStarted) {
        if (lobbyState?.isStarted == true) {
            onGameStarted(gameId)
        }
    }

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
        lobbyState?.let { lobby ->
            val currentPlayer = userSessionVm.username.value.orEmpty()
            val currentRole = lobby.selectedRoles[currentPlayer]
            val mrXTaken = lobby.selectedRoles.any { it.value == "MRX" && it.key != currentPlayer }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Text("Spieler in der Lobby:", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    lobby.players.forEach { p ->
                        val ready = lobby.readyStatus[p] == true
                        val role = lobby.selectedRoles[p] ?: "unbekannt"
                        Text("• $p - Rolle: $role ${if (ready) "(bereit)" else "(wartet)"}")
                    }
                }


                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 24.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Wähle deine Rolle:", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = { lobbyVm.selectRole(gameId, currentPlayer, "MRX") },
                            enabled = !mrXTaken && !lobby.isStarted
                        ) {
                            Text("MrX")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { lobbyVm.selectRole(gameId, currentPlayer, "DETECTIVE") },
                            enabled = !lobby.isStarted
                        ) {
                            Text("Detective")
                        }
                    }

                    currentRole?.let {
                        Spacer(Modifier.height(8.dp))
                        Text("Deine aktuelle Rolle: $it")
                    }

                    if (mrXTaken && currentRole != "MRX") {
                        Text(
                            text = "MrX ist bereits vergeben",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                    if (!lobby.isStarted) {
                        Button(
                            onClick = {
                                lobbyVm.sendReady(gameId, currentPlayer)
                            }
                        ) {
                            Text("Bereit")
                        }
                    } else {
                        Text("Das Spiel hat begonnen!", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        } ?: Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
