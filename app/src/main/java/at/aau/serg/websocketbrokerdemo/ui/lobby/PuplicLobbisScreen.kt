package at.aau.serg.websocketbrokerdemo.ui.lobby

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.aau.serg.websocketbrokerdemo.data.model.LobbyState
import at.aau.serg.websocketbrokerdemo.viewmodel.LobbyViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicLobbiesScreen(
    onSelect: (String) -> Unit,
    onBack: () -> Unit,
    lobbyViewModel: LobbyViewModel = viewModel(),
    snackbarHostState: SnackbarHostState
) {

    val lobbies by lobbyViewModel.publicLobbies.collectAsState(initial = emptyList())


    // beim ersten Composen Daten laden
    LaunchedEffect(Unit) {
        lobbyViewModel.fetchPublicLobbies()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Öffentliche Lobbys") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zurück"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (lobbies.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Keine öffentlichen Lobbys vorhanden")
            }
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                items(lobbies) { lobby ->
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onSelect(lobby.gameId) }
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("ID: ${lobby.gameId}", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Spieler: ${lobby.currentPlayerCount}/${lobby.maxPlayers}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
