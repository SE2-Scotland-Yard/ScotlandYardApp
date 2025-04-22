package at.aau.serg.websocketbrokerdemo.ui.lobby

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.aau.serg.websocketbrokerdemo.viewmodel.UserSessionViewModel

@Composable
fun LobbyMenuScreen(
    onCreateLobby: (Boolean) -> Unit,
    onJoinLobby: () -> Unit,
    onFindPublicLobbies: () -> Unit,
    userSession: UserSessionViewModel
) {


    val username = userSession.username.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Willkommen, $username", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onCreateLobby(true) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Öffentliche Lobby erstellen")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onCreateLobby(false) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Private Lobby erstellen")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onJoinLobby, modifier = Modifier.fillMaxWidth()) {
            Text("Einer Lobby beitreten")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onFindPublicLobbies, modifier = Modifier.fillMaxWidth()) {
            Text("Öffentliche Lobbys anzeigen")
        }
    }
}
