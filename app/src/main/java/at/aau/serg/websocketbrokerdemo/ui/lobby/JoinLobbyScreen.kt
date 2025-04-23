package at.aau.serg.websocketbrokerdemo.ui.lobby

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinLobbyScreen(
    onJoin: (String) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var gameId by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lobby beitreten") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zurück"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = gameId,
                onValueChange = { gameId = it },
                label = { Text("Lobby‑ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onJoin(gameId.trim()) },
                modifier = Modifier.fillMaxWidth(),
                enabled = gameId.isNotBlank()
            ) {
                Text("Beitreten")
            }
        }
    }
}
