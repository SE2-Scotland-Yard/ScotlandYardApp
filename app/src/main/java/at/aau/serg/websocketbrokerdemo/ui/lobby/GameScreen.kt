package at.aau.serg.websocketbrokerdemo.ui.lobby

import GameViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.websocketbrokerdemo.viewmodel.LobbyViewModel
import at.aau.serg.websocketbrokerdemo.viewmodel.UserSessionViewModel
import com.example.myapplication.R
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TextFieldDefaults.indicatorLine
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import at.aau.serg.websocketbrokerdemo.data.model.AllowedMoveResponse
import at.aau.serg.websocketbrokerdemo.data.model.GameUpdate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    gameId: String,
    lobbyVm: LobbyViewModel,
    userSessionVm: UserSessionViewModel,
    gameVm: GameViewModel,
    useSmallMap: Boolean = false

) {
    val username = userSessionVm.username.value
    val gameUpdate by lobbyVm.gameState.collectAsState()
    val message by remember { derivedStateOf { gameVm.message } }
    val allowedMoves by remember { derivedStateOf { gameVm.allowedMoves } }
    val mrXPosition by remember { derivedStateOf { gameVm.mrXPosition } }
    val allowedMovesDetails by remember { derivedStateOf { gameVm.allowedMovesDetails } }
    val error by remember { derivedStateOf { gameVm.errorMessage } }

    var expanded by remember { mutableStateOf(false) }
    var selectedMove by remember { mutableStateOf<Int?>(null) }

    val isMyTurn = username == gameUpdate?.currentPlayer

    // Moves nach dem Join laden
    LaunchedEffect(gameId, username) {
        if (username != null) {
            gameVm.fetchAllowedMoves(gameId, username)
            gameVm.fetchMrXPosition(gameId, username)
        }
    }

    LaunchedEffect(gameUpdate?.currentPlayer) {
        if (username != null && gameUpdate?.currentPlayer == username) {
            gameVm.fetchAllowedMoves(gameId, username)
            gameVm.fetchMrXPosition(gameId, username)
        }
    }


    Scaffold { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
        ) {
            Map(gameVm, useSmallMap)

            OverlayLeft(username, userSessionVm, mrXPosition, gameUpdate, message, gameId)
            OverlayRight(error, expanded, isMyTurn, selectedMove, allowedMoves, username, gameVm, gameId )
        }
    }
}

@Composable
private fun BoxScope.OverlayRight(
    error: String?,
    expanded: Boolean,
    isMyTurn: Boolean,
    selectedMove: Int?,
    allowedMoves: List<AllowedMoveResponse>,
    username: String?,
    gameVm: GameViewModel,
    gameId: String
) {
    var expanded1 = expanded
    var selectedMove1 = selectedMove
    Column(
        modifier = Modifier
            .align(Alignment.TopEnd) // Unten rechts ausrichten
            .padding(24.dp)
            .width(IntrinsicSize.Max) // Passt die Breite an den breitesten Inhalt an
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)) // Halbtransparenter Hintergrund
            .padding(16.dp)
            .zIndex(1f) // Stellt sicher, dass dieses Overlay über der Karte liegt
    ) {
        Text("Erlaubte Züge:", style = MaterialTheme.typography.titleMedium)

        if (error != null) {
            Text("Fehler: $error", color = MaterialTheme.colorScheme.error)
        } else {
            Box {
                Button(
                    onClick = { expanded1 = true },
                    enabled = isMyTurn
                ) {
                    Text(selectedMove1?.let { "Feld $it gewählt" } ?: "Zugziel wählen")
                }
                DropdownMenu(expanded = expanded1, onDismissRequest = { expanded1 = false }) {
                    if (allowedMoves.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Keine Züge verfügbar") },
                            onClick = { expanded1 = false }
                        )
                    } else {
                        allowedMoves.forEach { move ->
                            val ticketId = move.keys.first()
                            val ticketType = move.values.first()
                            DropdownMenuItem(
                                text = { Text("$ticketType (Position zu: $ticketId)") },
                                onClick = {
                                    selectedMove1 = ticketId
                                    expanded1 = false
                                    username?.let { name ->
                                        gameVm.move(gameId, name, ticketId, ticketType)
                                        Thread.sleep(2000L)
                                        gameVm.fetchMrXPosition(gameId, username)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.OverlayLeft(
    username: String?,
    userSessionVm: UserSessionViewModel,
    mrXPosition: Int?,
    gameUpdate: GameUpdate?,
    message: String,
    gameId: String

) {
    Column(
        modifier = Modifier
            .align(Alignment.TopStart) // Oben links ausrichten
            .padding(24.dp)
            .width(IntrinsicSize.Max) // Passt die Breite an den breitesten Inhalt an
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)) // Halbtransparenter Hintergrund
            .padding(16.dp)
            .zIndex(1f) // Stellt sicher, dass dieses Overlay über der Karte liegt
    ) {
        gameId.let {
            Text("Game ID: $it", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
        }
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

        if (userSessionVm.role.value == "MRX") {
            mrXPosition?.let {
                Text("MrX steht auf: $it")
            }
            Spacer(Modifier.height(16.dp))
            gameUpdate?.playerPositions?.forEach { (name, pos) ->
                Text("$name steht auf Feld $pos")
            }
        } else {
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

@Composable
fun Map(gameVm: GameViewModel,useSmallMap: Boolean) {
    var scale by remember { mutableFloatStateOf(1f) }

    val mapPainter = painterResource(id = if (useSmallMap) R.drawable.map_small else R.drawable.map)
    val intrinsicSize = mapPainter.intrinsicSize
    val points = gameVm.pointPositions

    val scrollStateX = rememberScrollState()
    val scrollStateY = rememberScrollState()

    val density = LocalDensity.current
    val virtualWidthDp = with(density) { (intrinsicSize.width * scale).toDp() }
    val virtualHeightDp = with(density) { (intrinsicSize.height * scale).toDp() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),

    ) {
        // Scrollable container
        Box(
            modifier = Modifier
                .horizontalScroll(scrollStateX)
                .verticalScroll(scrollStateY)
                .align(Alignment.Center)
        ) {
            // Content sized to scaled dimensions
            Box(
                modifier = Modifier
                    .size(virtualWidthDp, virtualHeightDp) // Scaled size
            ) {
                Image(
                    painter = mapPainter,
                    contentDescription = "Map",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize() // fill scaled box
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    points.forEach { (_, pos) ->
                        val(x,y) = pos
                        drawCircle(
                            color = Color.Red,
                            radius = 15f,
                            center = Offset(x * scale, y* scale)
                        )
                    }
                }
            }
        }

        // Zoom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Slider(
                value = scale,
                onValueChange = { scale = it },
                valueRange = 0.5f..5f
            )
            Button(
                onClick = { scale = 1f },
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.buttonStartScreen))
            ) {
                Text("Reset Zoom")
            }
        }
    }
}





