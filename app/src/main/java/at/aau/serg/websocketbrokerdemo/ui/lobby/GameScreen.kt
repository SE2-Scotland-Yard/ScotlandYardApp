package at.aau.serg.websocketbrokerdemo.ui.lobby

import GameViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import at.aau.serg.websocketbrokerdemo.data.model.MrXDoubleMoveResponse
import androidx.compose.ui.unit.sp
import at.aau.serg.websocketbrokerdemo.viewmodel.LobbyViewModel
import at.aau.serg.websocketbrokerdemo.viewmodel.UserSessionViewModel
import com.example.myapplication.R


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
    val allowedDoubleMoves by remember { derivedStateOf { gameVm.allowedDoubleMoves } }
    val isDoubleMoveMode by remember { derivedStateOf { gameVm.isDoubleMoveMode } }

    var expanded by remember { mutableStateOf(false) }
    var selectedMove by remember { mutableStateOf<Int?>(null) }

    //States für DoubleMove
    var firstMoveSelected by remember { mutableStateOf<MrXDoubleMoveResponse?>(null) }
    var secondMoveSelected by remember { mutableStateOf<MrXDoubleMoveResponse?>(null) }
    var expandedFirstMove by remember { mutableStateOf(false) }
    var expandedSecondMove by remember { mutableStateOf(false) }


    val isMyTurn = username == gameUpdate?.currentPlayer

    // Moves nach dem Join laden
    LaunchedEffect(gameId, username) {
        if (username != null) {
            gameVm.fetchAllowedMoves(gameId, username)
            gameVm.fetchMrXPosition(gameId, username)
            gameVm.fetchMrXPosition(gameId, username)
            if (userSessionVm.role.value == "MRX") {
                gameVm.fetchAllowedDoubleMoves(gameId, username)
                //zuerst auf false, MrX setzt selber
                gameVm.updateDoubleMoveMode(false)
            }
        }
    }

    LaunchedEffect(gameUpdate?.currentPlayer) {
        if (username != null && gameUpdate?.currentPlayer == username) {
            gameVm.fetchAllowedMoves(gameId, username)
            gameVm.fetchMrXPosition(gameId, username)
            gameVm.fetchMrXPosition(gameId, username)
            if (userSessionVm.role.value == "MRX") {
                gameVm.fetchAllowedDoubleMoves(gameId, username)

            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Game $gameId") })
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Linke Seite: Spielerstatus
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
            //Map

            var scale by remember { mutableFloatStateOf(1f) }
            var offsetX by remember { mutableFloatStateOf(0f) }
            var offsetY by remember { mutableFloatStateOf(0f) }
            val points = gameVm.pointPositions

            Box(
                modifier = Modifier
                    .weight(3f)
                    .background(color = colorResource(R.color.light_blue_900))
                    .clipToBounds()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            offsetX += pan.x
                            offsetY += pan.y
                        }
                    }
            ) {
                Image(
                    painter = painterResource(id = if (useSmallMap) R.drawable.map_small else R.drawable.map),
                    contentDescription = "Scotland Yard Map",
                    modifier = Modifier
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )

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
                    ) {
                        Text(text = "Reset Zoom", fontSize = 12.sp)
                    }
                }
            }

            // Rechte Seite: Auswahl & Bewegung
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text("Erlaubte Züge:", style = MaterialTheme.typography.titleMedium)

                    if (error != null) {
                        Text("Fehler: $error", color = MaterialTheme.colorScheme.error)
                    } else {
                        Box {
                            Button(
                                onClick = { expanded = true },
                                enabled = isMyTurn
                            ) {
                                Text(selectedMove?.let { "Feld $it gewählt" } ?: "Zugziel wählen")
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                if (allowedMoves.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("Keine Züge verfügbar") },
                                        onClick = { expanded = false }
                                    )
                                } else {
                                    allowedMoves.forEach { move ->
                                        val ticketId = move.keys.first()
                                        val ticketType = move.values.first()
                                        DropdownMenuItem(
                                            text = { Text("$ticketType (Position zu: $ticketId)") },
                                            onClick = {
                                                selectedMove = ticketId
                                                expanded = false
                                                username?.let { name ->
                                                    gameVm.move(gameId, name, ticketId, ticketType)
                                                    Thread.sleep(2000L)
                                                    gameVm.fetchMrXPosition(gameId, name)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        if (userSessionVm.role.value == "MRX") {
                            Button(
                                onClick = {
                                    gameVm.updateDoubleMoveMode(!isDoubleMoveMode)
                                    firstMoveSelected = null
                                    secondMoveSelected = null
                                    selectedMove = null
                                },
                                enabled = isMyTurn,

                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (isDoubleMoveMode) "Doppelzugmodus deaktivieren" else "Doppelzugmodus aktivieren")
                            }

                            Spacer(Modifier.height(24.dp))

                            if (isDoubleMoveMode) {
                                // Erster Zug
                                Box {
                                    Button(onClick = { expandedFirstMove = true }) {
                                        Text(firstMoveSelected?.let { "Erster Zug: Feld ${it.firstTo}" }
                                            ?: "Ersten Zug wählen")
                                    }
                                    DropdownMenu(
                                        expanded = expandedFirstMove,
                                        onDismissRequest = { expandedFirstMove = false }) {
                                        if (allowedDoubleMoves.isEmpty()) {
                                            DropdownMenuItem(
                                                text = { Text("Keine Doppelzüge verfügbar") },
                                                onClick = { expandedFirstMove = false }
                                            )
                                        } else {
                                            allowedDoubleMoves.forEach { move ->
                                                DropdownMenuItem(
                                                    text = { Text("Feld ${move.firstTo} mit ${move.firstTicket}") },
                                                    onClick = {
                                                        firstMoveSelected = move
                                                        expandedFirstMove = false
                                                        secondMoveSelected = null
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(16.dp))

                                if (firstMoveSelected != null) {
                                    Box {
                                        Button(onClick = { expandedSecondMove = true }) {
                                            Text(secondMoveSelected?.let { "Zweiter Zug: Feld ${it.secondTo}" }
                                                ?: "Zweiten Zug wählen")
                                        }
                                        DropdownMenu(
                                            expanded = expandedSecondMove,
                                            onDismissRequest = { expandedSecondMove = false }) {
                                            allowedDoubleMoves
                                                .filter {
                                                    it.firstTo == firstMoveSelected!!.firstTo &&
                                                            it.firstTicket == firstMoveSelected!!.firstTicket
                                                }
                                                .forEach { move ->
                                                    DropdownMenuItem(
                                                        text = { Text("Feld ${move.secondTo} mit ${move.secondTicket}") },
                                                        onClick = {
                                                            secondMoveSelected = move
                                                            expandedSecondMove = false
                                                        }
                                                    )
                                                }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        if (firstMoveSelected != null && secondMoveSelected != null && username != null) {
                                            gameVm.moveDouble(
                                                gameId,
                                                username,
                                                firstMoveSelected!!.firstTo,
                                                firstMoveSelected!!.firstTicket,
                                                secondMoveSelected!!.secondTo,
                                                secondMoveSelected!!.secondTicket
                                            )
                                            gameVm.fetchMrXPosition(gameId, username)
                                            gameVm.fetchAllowedDoubleMoves(gameId, username)
                                            gameVm.updateDoubleMoveMode(false)
                                            firstMoveSelected = null
                                            secondMoveSelected = null
                                        }
                                    },
                                    enabled = firstMoveSelected != null && secondMoveSelected != null
                                ) {
                                    Text("Doppelzug ausführen")
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}


