package at.aau.serg.websocketbrokerdemo.ui.lobby

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.aau.serg.websocketbrokerdemo.viewmodel.UserSessionViewModel
import com.example.myapplication.R

@Composable
fun LobbyMenuScreen(
    onCreateLobby: (Boolean) -> Unit,
    onJoinLobby: () -> Unit,
    onFindPublicLobbies: () -> Unit,
    userSession: UserSessionViewModel
) {


    val username = userSession.username.value

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background1),
            contentDescription = "Lobby Hintergrund",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Willkommen, $username",
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.85f),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onCreateLobby(true) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.buttonStartScreen),
                contentColor = Color.White
            )
        ) {
            Text("Öffentliche Lobby erstellen")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onCreateLobby(false) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.buttonStartScreen),
                contentColor = Color.White
            )
        ) {
            Text("Private Lobby erstellen")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onJoinLobby,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.buttonStartScreen),
                contentColor = Color.White
            )
        ) {
            Text("Einer Lobby beitreten")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onFindPublicLobbies,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.buttonStartScreen),
                contentColor = Color.White
            )
        ) {
            Text("Öffentliche Lobbys anzeigen")
        }
    }
}
