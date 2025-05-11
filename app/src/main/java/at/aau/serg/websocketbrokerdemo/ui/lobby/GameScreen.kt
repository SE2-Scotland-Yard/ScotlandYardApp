package at.aau.serg.websocketbrokerdemo.ui.lobby

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.aau.serg.websocketbrokerdemo.viewmodel.LobbyViewModel
import at.aau.serg.websocketbrokerdemo.viewmodel.UserSessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(gameId: String,
               lobbyVm: LobbyViewModel,
               userSessionVm: UserSessionViewModel){

    LaunchedEffect(gameId) {
        lobbyVm.fetchLobbyStatus(gameId)

    }

    val lobbyState by lobbyVm.lobbyStatus.collectAsState()
    val username = userSessionVm.username.value
    val position = lobbyVm

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game $gameId") },

            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            lobbyState?.let { lobby ->
                // Hole die benötigten Daten aus dem Lobby-State
                val currentRole = lobby.selectedRoles[username]
                val players = lobby.players

                // Zeige die Daten an, falls vorhanden
                username?.let {
                    Text("Dein Username: $it")
                    Spacer(Modifier.height(8.dp))
                }

                currentRole?.let {
                    Text("Deine aktuelle Rolle: $it")
                    Spacer(Modifier.height(8.dp))
                }

                players?.let {
                    Text("Deine aktuelle Position: $it")
                    Spacer(Modifier.height(8.dp))
                }
            }
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
                    }

                }
            }


        }
    }}


