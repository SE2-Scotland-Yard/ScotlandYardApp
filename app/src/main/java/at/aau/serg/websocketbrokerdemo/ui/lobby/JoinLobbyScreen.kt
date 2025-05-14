package at.aau.serg.websocketbrokerdemo.ui.lobby

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinLobbyScreen(
    onJoin: (String) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var gameId by remember { mutableStateOf("") }

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
                title = { Text("Lobby beitreten",
                    fontWeight = FontWeight.Bold) },
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
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White.copy(alpha = 0.85f),
                    focusedContainerColor = Color.White.copy(alpha = 0.95f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onJoin(gameId.trim()) },
                modifier = Modifier.fillMaxWidth(),
                enabled = gameId.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.buttonStartScreen),
                    contentColor = Color.White.copy(alpha = 0.85f)
                )
            ) {
                Text("Beitreten")
            }
        }
    }
}

