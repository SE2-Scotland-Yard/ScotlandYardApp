package at.aau.serg.websocketbrokerdemo.ui.lobby


import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import at.aau.serg.websocketbrokerdemo.model.Avatar
import at.aau.serg.websocketbrokerdemo.viewmodel.LobbyViewModel
import at.aau.serg.websocketbrokerdemo.viewmodel.UserSessionViewModel
import com.example.myapplication.R
import kotlinx.coroutines.delay


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

    var showAvatarPicker by remember { mutableStateOf(false) }

    val avatarsMap = lobbyState?.avatars
    val currentUsername = userSessionVm.username.value



// Zeit der letzten Interaktion
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }

// Dialog anzeigen
    var showIdleDialog by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }




    val error by lobbyVm.errorMessage.collectAsState()

    // Zeige Fehler-Toast
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            lobbyVm.clearError()
        }
    }


    //Starte das Spiel, wenn "isStarted" true wird
    LaunchedEffect(lobbyState?.isStarted) {
        if (lobbyState?.isStarted == true) {
            onGameStarted(gameId)
        }
    }
    // lobbyStatus holen + websocket verbinden
    LaunchedEffect(gameId) {

        lobbyVm.fetchLobbyStatus(gameId)

        // Mit der Lobby verbinden
        lobbyVm.connectToLobby(
            gameId,
            context = context,
            onConnected = {

                val player = userSessionVm.username.value
                if (!player.isNullOrBlank()) {
                    lobbyVm.sendPingToLobby(gameId, player)
                }
            }
        )

        // Sofortigen Ping ohne Callback senden
        val player = userSessionVm.username.value
        if (!player.isNullOrBlank()) {
            lobbyVm.sendPingToLobby(gameId, player)
        }
    }





    // Synchronisiere Rolle und Avatar aus dem LobbyState ins lokale UserSessionViewModel
    LaunchedEffect(lobbyState) {
        val currentPlayer = userSessionVm.username.value.orEmpty()

        // Eigene Rolle setzen
        userSessionVm.role.value = lobbyState?.selectedRoles?.get(currentPlayer)

        // Eigenen Avatar zurücksetzen, wenn im Backend nicht mehr vorhanden
        if (lobbyState?.avatars?.get(currentPlayer) == null) {
            userSessionVm.avatarResId = null
        }

        //Globale Avatare aller Spieler merken
        lobbyState?.avatars?.let { avatarMap ->
            userSessionVm.avatarIds.clear()
            userSessionVm.avatarIds.putAll(avatarMap)
        }

        //Globale Rollen aller Spieler merken
        lobbyState?.selectedRoles?.let { roleMap ->
            userSessionVm.roles.clear()
            userSessionVm.roles.putAll(roleMap)
        }
    }
    //Inaktivität  abfragen
    LaunchedEffect(Unit) {
        while (true) {
            delay(10_000) // alle 10 Sekunden checken
            val now = System.currentTimeMillis()
            if (now - lastInteractionTime > 2 * 60 * 1000 && !showIdleDialog) {
                showIdleDialog = true
            }
        }
    }
    //nach 2min30 inaktivität spieler kicken
    LaunchedEffect(showIdleDialog) {
        if (showIdleDialog) {
            delay(30_000) // 30 Sekunden zum Reagieren
            if (showIdleDialog) {
                val player = userSessionVm.username.value.orEmpty()
                lobbyVm.sendLeave(gameId, player) {
                    onLeft()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000) // alle 30 Sekunden
            val player = userSessionVm.username.value ?: continue
            lobbyVm.sendPingToLobby(gameId, player)
        }
    }






    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background1),
            contentDescription = "Lobby Hintergrund",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        //Avatar unten links
        if (userSessionVm.avatarResId != null && userSessionVm.role.value != "MRX") {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = userSessionVm.avatarResId!!),
                    contentDescription = "Dein Avatar",
                    modifier = Modifier
                        .size(64.dp)
                        .border(2.dp, Color.White, CircleShape)
                        .background(Color.Black.copy(alpha = 0.2f), CircleShape)
                        .padding(4.dp),
                    contentScale = ContentScale.Crop
                )

            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Lobby $gameId",
                        //style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { showSettingsMenu = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_settings),
                            contentDescription = "Einstellungen"
                        )
                    }

                    DropdownMenu(
                        expanded = showSettingsMenu,
                        onDismissRequest = { showSettingsMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Bot hinzufügen") },
                            onClick = {
                                showSettingsMenu = false
                                lobbyVm.addBotToLobby(gameId)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Bot entfernen") },
                            onClick = {
                                showSettingsMenu = false
                                lobbyVm.removeBotFromLobby(gameId)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Verlassen") },
                            onClick = {
                                showSettingsMenu = false
                                val player = userSessionVm.username.value.orEmpty()
                                lobbyVm.sendLeave(gameId, player) {
                                    onLeft()
                                }
                            }
                        )
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
                    Text(
                        "Spieler in der Lobby:",
                        //style = MaterialTheme.typography.titleMedium
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(Modifier.height(8.dp))

                    lobby.players.forEach { p ->
                        val ready = lobby.readyStatus[p] == true
                        val role = lobby.selectedRoles[p] ?: "unbekannt"
                        Text(
                            "• $p - Rolle: $role ${if (ready) "(bereit)" else "(wartet)"}",
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
                        Text(
                            "Wähle deine Rolle:",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {

                                lobbyVm.selectRole(gameId, currentPlayer, "MRX",)
                                lastInteractionTime = System.currentTimeMillis()
                            },
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
                                lastInteractionTime = System.currentTimeMillis()
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
                        Text(
                            "Deine aktuelle Rolle: $it",
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
                    val hasRole = currentRole != null
                    val hasAvatar =
                        userSessionVm.role.value == "MRX" || userSessionVm.avatarResId != null
                    val isReadyEnabled = !lobby.isStarted && hasRole && hasAvatar

                    if (!lobby.isStarted) {
                        Button(
                            onClick = {
                                lobbyVm.sendReady(gameId, currentPlayer)
                                lastInteractionTime = System.currentTimeMillis()
                            },
                            enabled = isReadyEnabled,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.buttonStartScreen),
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Bereit")
                        }

                        if (!hasRole) {
                            Text(
                                text = "Bitte zuerst eine Rolle wählen",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else if (!hasAvatar && userSessionVm.role.value == "DETECTIVE") {
                            Text(
                                text = "Bitte einen Avatar wählen",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }




                    Button(
                        onClick = {

                            showAvatarPicker = !showAvatarPicker
                            lastInteractionTime = System.currentTimeMillis()

                        },
                        enabled = userSessionVm.role.value == "DETECTIVE",
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.buttonStartScreen),
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Avatar wählen")
                    }

                }
            }
        } ?: Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        val avatars = Avatar.values().toList()


        var selectedAvatar by remember { mutableStateOf(userSessionVm.avatarResId) }


        if (showAvatarPicker && userSessionVm.role.value == "DETECTIVE") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),

                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .background(Color.DarkGray, shape = MaterialTheme.shapes.medium)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Wähle deinen Avatar:",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        avatars.forEach { avatar ->
                            val isSelected = avatar.drawableRes == selectedAvatar
                            val isTaken = avatarsMap?.values?.contains(avatar.id) == true &&
                                    avatarsMap[currentUsername] != avatar.id

                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(64.dp)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = when {
                                            isTaken -> Color.Red
                                            isSelected -> Color.Yellow
                                            else -> Color.White
                                        },
                                        shape = CircleShape
                                    )
                                    .clickable(enabled = !isTaken) {
                                        selectedAvatar = avatar.drawableRes
                                        userSessionVm.avatarResId = avatar.drawableRes
                                        lobbyVm.selectAvatar(gameId, currentUsername!!, avatar.id)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = avatar.drawableRes),
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { showAvatarPicker = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                    ) {
                        Text("Fertig")
                    }
                }
            }
        }


    }

    if (showIdleDialog) {
        AlertDialog(
            onDismissRequest = {},

            title = { Text("Noch da?") },
            text = { Text("Du warst eine Weile inaktiv. Bist du noch da?") },

            confirmButton = {
                TextButton(onClick = {
                    lastInteractionTime = System.currentTimeMillis()
                    showIdleDialog = false
                }) {
                    Text("Ja")
                }
            },

            dismissButton = {
                TextButton(onClick = {
                    val player = userSessionVm.username.value.orEmpty()
                    lobbyVm.sendLeave(gameId, player) {
                        onLeft()
                    }
                }) {
                    Text("Nein, verlassen")
                }
            }
        )
    }


}