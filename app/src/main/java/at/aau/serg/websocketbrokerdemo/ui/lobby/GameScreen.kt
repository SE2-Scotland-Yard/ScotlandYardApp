package at.aau.serg.websocketbrokerdemo.ui.lobby

import GameViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.aau.serg.websocketbrokerdemo.data.model.MrXDoubleMoveResponse
import at.aau.serg.websocketbrokerdemo.viewmodel.LobbyViewModel
import at.aau.serg.websocketbrokerdemo.viewmodel.UserSessionViewModel


@OptIn(ExperimentalMaterial3Api::class)
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

            Spacer(Modifier.width(24.dp)) // Abstand

            // Rechte Seite: Auswahl & Bewegung
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
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
                        // Doppelzugmodus-Umschalter Button
                        Button(
                            onClick = {
                                gameVm.updateDoubleMoveMode(!isDoubleMoveMode)
                                // Auswahl resetten, wenn Mode wechselt
                                firstMoveSelected = null
                                secondMoveSelected = null
                                selectedMove = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isDoubleMoveMode) "Doppelzugmodus deaktivieren" else "Doppelzugmodus aktivieren")
                        }
                        Spacer(Modifier.height(24.dp))

                        if (isDoubleMoveMode && userSessionVm.role.value == "MRX") {
                            // Doppelzug Auswahl
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

                            // Zweiter Zug Dropdown, nur sichtbar wenn erster Zug ausgewählt wurde
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
                                            .filter { it.firstTo == firstMoveSelected!!.firstTo && it.firstTicket == firstMoveSelected!!.firstTicket }
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

                            // Button zum Bestätigen des Doppelzugs
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
                                        // Danach Status aktualisieren
                                        gameVm.fetchMrXPosition(gameId, username)
                                        gameVm.fetchAllowedDoubleMoves(gameId, username)
                                        // DoubleMove Mode aus
                                        gameVm.updateDoubleMoveMode(false)

                                        // Reset der Auswahl
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






