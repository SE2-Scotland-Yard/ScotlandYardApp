package at.aau.serg.websocketbrokerdemo.ui.game

import androidx.compose.runtime.Composable
import GameViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.aau.serg.websocketbrokerdemo.viewmodel.LobbyViewModel
import at.aau.serg.websocketbrokerdemo.viewmodel.UserSessionViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp


import com.example.myapplication.R


@Composable
fun GameScreen(
    gameId: String,
    lobbyVm: LobbyViewModel,
    userSessionVm: UserSessionViewModel,
    gameVm: GameViewModel
) {
    val username = userSessionVm.username.value
    val gameUpdate by lobbyVm.gameState.collectAsState()
    val message by remember { derivedStateOf { gameVm.message } }
    val allowedMoves by remember { derivedStateOf { gameVm.allowedMoves } }
    val mrXPosition by remember { derivedStateOf { gameVm.mrXPosition }}
    val allowedMovesDetails by remember { derivedStateOf { gameVm.allowedMovesDetails }}
    val error by remember { derivedStateOf { gameVm.errorMessage } }

    var expanded by remember { mutableStateOf(false) }
    var selectedMove by remember { mutableStateOf<Int?>(null) }

    val isMyTurn = username == gameUpdate?.currentPlayer

    // Moves nach dem Join laden
    LaunchedEffect(gameId, username) {
        if (username != null) {
            gameVm.fetchAllowedMoves(gameId, username)
            gameVm.fetchMrXPosition(gameId,username)
        }
    }

    LaunchedEffect(gameUpdate?.currentPlayer) {
        if (username != null && gameUpdate?.currentPlayer == username) {
            gameVm.fetchAllowedMoves(gameId, username)
            gameVm.fetchMrXPosition(gameId,username)
        }
    }
}

@Composable
fun Map(
    gameId: String,
    lobbyVm: LobbyViewModel,
    userSessionVm: UserSessionViewModel,
    gameVm: GameViewModel,
    useSmallMap: Boolean = false
){
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Image(
            painter = painterResource(id = if (useSmallMap) R.drawable.map_small else R.drawable.map),
            contentDescription = "Scotland Yard Map",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Button(
                onClick = {
                    scale = 1f
                    offsetX = 0f
                    offsetY = 0f
                },
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.buttonStartScreen)),
                modifier = Modifier
                    .size(width = 150.dp, height = 50.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = Color.Black,
                        spotColor = Color.DarkGray
                    )
            ) {
                Text("Reset Zoom")
            }
        }
    }
}

@Composable
fun BottomBar(
    gameId: String,
    lobbyVm: LobbyViewModel,
    userSessionVm: UserSessionViewModel,
    gameVm: GameViewModel
){

}

@Composable
fun SideBar(
    gameId: String,
    lobbyVm: LobbyViewModel,
    userSessionVm: UserSessionViewModel,
    gameVm: GameViewModel
){
    val username = userSessionVm.username.value
    val gameUpdate by lobbyVm.gameState.collectAsState()
    val message by remember { derivedStateOf { gameVm.message } }
    val allowedMoves by remember { derivedStateOf { gameVm.allowedMoves } }
    val mrXPosition by remember { derivedStateOf { gameVm.mrXPosition }}
    val allowedMovesDetails by remember { derivedStateOf { gameVm.allowedMovesDetails }}
    val error by remember { derivedStateOf { gameVm.errorMessage } }

    var expanded by remember { mutableStateOf(false) }
    var selectedMove by remember { mutableStateOf<Int?>(null) }

    val isMyTurn = username == gameUpdate?.currentPlayer

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
    ) {
        username?.let {
            Text("Eingeloggt als: $it", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
        }
        userSessionVm.role.value?.let {
            Text("Du bist: $it", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
        }

        Text("Spielerpositionen:", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))


        if (userSessionVm.role.value=="MRX") {
            mrXPosition?.let {
                Text("MrX steht auf: $it")
            }
            Spacer(Modifier.height(16.dp))
            gameUpdate?.playerPositions?.forEach { (name, pos) ->
                Text("$name steht auf Feld $pos")
            }
        }else {
            gameUpdate?.playerPositions?.forEach { (name, pos) ->
                Text("$name steht auf Feld $pos")
            }
        }
        Spacer(Modifier.height(4.dp))
        val myPosition = gameUpdate?.playerPositions?.get(username)
        myPosition?.let {
            Spacer(Modifier.height(16.dp))
            Text("➡ Du stehst auf Feld $it", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
        }

        // Hier wird die Nachricht angezeigt
        if (message.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(message, color = MaterialTheme.colorScheme.primary)
        }
    }
}