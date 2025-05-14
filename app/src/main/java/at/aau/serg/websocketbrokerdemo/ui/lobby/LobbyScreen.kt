package at.aau.serg.websocketbrokerdemo.ui.lobby

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.aau.serg.websocketbrokerdemo.viewmodel.LobbyViewModel
import at.aau.serg.websocketbrokerdemo.viewmodel.UserSessionViewModel
import com.example.myapplication.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    gameId: String,
    lobbyVm: LobbyViewModel,
    userSessionVm: UserSessionViewModel,
    onLeft: () -> Unit,
    onGameStarted: (String) -> Unit,

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

    LaunchedEffect(lobbyState) {
        val currentPlayer = userSessionVm.username.value.orEmpty()
        val currentRole = lobbyState?.selectedRoles?.get(currentPlayer)
        if (currentRole != null) {
            userSessionVm.role.value = currentRole
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background1),
            contentDescription = "Lobby Hintergrund",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Lobby $gameId",
                    //style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(
                        onClick = {
                            val player = userSessionVm.username.value.orEmpty()
                            lobbyVm.leaveLobby(gameId, player) {
                                onLeft()
                            }
                        }
                    ) {
                        Text("Verlassen",
                            color = Color.Black)
                    }
                }
            )
        }
    ) { padding ->
        lobbyState?.let { lobby ->
            val currentPlayer = userSessionVm.username.value.orEmpty()
            val currentRole = lobby.selectedRoles[currentPlayer]
            val mrXTaken =
                lobby.selectedRoles.any { it.value == "MRX" && it.key != currentPlayer }

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
                    Text("Spieler in der Lobby:",
                        //style = MaterialTheme.typography.titleMedium
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(Modifier.height(8.dp))

                    lobby.players.forEach { p ->
                        val ready = lobby.readyStatus[p] == true
                        val role = lobby.selectedRoles[p] ?: "unbekannt"
                        Text("• $p - Rolle: $role ${if (ready) "(bereit)" else "(wartet)"}",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.titleMedium
                        )
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
                        Text("Wähle deine Rolle:",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = { lobbyVm.selectRole(gameId, currentPlayer, "MRX") },
                            enabled = !mrXTaken && !lobby.isStarted,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.buttonStartScreen),
                                contentColor = Color.Black
                            )
                        ) {
                            Text("MrX")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                lobbyVm.selectRole(
                                    gameId,
                                    currentPlayer,
                                    "DETECTIVE"
                                )
                            },
                            enabled = !lobby.isStarted,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.buttonStartScreen),
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Detective")
                        }
                    }

                    currentRole?.let {
                        Spacer(Modifier.height(8.dp))
                        Text("Deine aktuelle Rolle: $it",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
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
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.buttonStartScreen),
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Bereit")
                        }
                    } else {
                        Text(
                            "Das Spiel hat begonnen!",
                            color = MaterialTheme.colorScheme.primary
                        )
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
