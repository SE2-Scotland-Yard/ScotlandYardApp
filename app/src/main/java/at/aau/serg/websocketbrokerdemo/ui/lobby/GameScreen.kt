package at.aau.serg.websocketbrokerdemo.ui.lobby

import GameViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.aau.serg.websocketbrokerdemo.viewmodel.LobbyViewModel
import at.aau.serg.websocketbrokerdemo.viewmodel.UserSessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    gameId: String,
    lobbyVm: LobbyViewModel,
    userSessionVm: UserSessionViewModel,
    gameVm:GameViewModel
) {
    val username = userSessionVm.username.value
    val gameUpdate by lobbyVm.gameState.collectAsState()
    val message = gameVm.message
    val allowedMoves = gameVm.allowedMoves
    val error = gameVm.errorMessage


    LaunchedEffect(gameId, username) {
        if (username != null) {
            gameVm.fetchAllowedMoves(gameId, username)
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game $gameId") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            username?.let {
                Text("Eingeloggt als: $it", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
            }

            Text("Spielerpositionen:", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))

            gameUpdate?.playerPositions?.forEach { (name, pos) ->
                Text("$name steht auf Feld $pos")
                Spacer(Modifier.height(4.dp))
            }

            val myPosition = gameUpdate?.playerPositions?.get(username)
            myPosition?.let {
                Spacer(Modifier.height(16.dp))
                Text("➡ Du stehst auf Feld $it", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
            }

            Text("Erlaubte Züge:", style = MaterialTheme.typography.titleMedium)
            if (error != null) {
                Text("Fehler: $error", color = MaterialTheme.colorScheme.error)
            } else {
                allowedMoves.forEach { pos ->
                    Text("→ Feld $pos")
                }
            }

            Button(
                onClick = {
                    username?.let {
                        gameVm.move(gameId, it, 1, "TAXI")
                    }
                },

            ) {
                Text("MOVE")
            }

            if (message.isNotEmpty()) {
                Text("Serverantwort: $message")
            }
        }
    }
}
