package at.aau.serg.websocketbrokerdemo.ui.lobby

import GameViewModel
import android.graphics.Color.alpha
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import at.aau.serg.websocketbrokerdemo.data.model.MrXDoubleMoveResponse
import at.aau.serg.websocketbrokerdemo.viewmodel.LobbyViewModel
import at.aau.serg.websocketbrokerdemo.viewmodel.UserSessionViewModel
import com.example.myapplication.R
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import at.aau.serg.websocketbrokerdemo.data.model.AllowedMoveResponse
import at.aau.serg.websocketbrokerdemo.viewmodel.Ticket
import kotlinx.coroutines.delay
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import kotlin.math.absoluteValue


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
    var visibleTicket by remember { mutableStateOf<String?>(null) }
    var showMrXHistory by remember { mutableStateOf(false) }
    var mrXHistory by remember { mutableStateOf<List<String>>(emptyList()) }



    //TODO import Double move from old Gamescreen
    //States für DoubleMove
    var firstMoveSelected by remember { mutableStateOf<MrXDoubleMoveResponse?>(null) }
    var secondMoveSelected by remember { mutableStateOf<MrXDoubleMoveResponse?>(null) }
    var expandedFirstMove by remember { mutableStateOf(false) }
    var expandedSecondMove by remember { mutableStateOf(false) }
    val playerPositions: Map<String, Int> = gameUpdate?.playerPositions ?: emptyMap()


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
    //Verwendetes Ticket anzeigen
    LaunchedEffect(gameUpdate) {
        val ticket = gameUpdate?.lastTicketUsed
        if (ticket != null) {
            visibleTicket = ticket
            delay(2000L)
            visibleTicket = ""
        }
    }
    //for mrX history
    @Composable
    fun TicketImage(ticket: String) {
        val ticketRes = when (ticket.uppercase()) {
            "TAXI" -> R.drawable.ticket_taxi
            "BUS" -> R.drawable.ticket_bus
            "UNDERGROUND" -> R.drawable.ticket_under
            "BLACK" -> R.drawable.ticket_black
            "DOUBLE" -> R.drawable.ticket_double
            else -> null
        }

        ticketRes?.let {
            Image(
                painter = painterResource(id = it),
                contentDescription = ticket,
                modifier = Modifier.size(32.dp)
            )
        }
    }




    LaunchedEffect(gameUpdate) {
        if (username != null) {

            gameVm.fetchMrXPosition(gameId, username)
            if (userSessionVm.role.value == "MRX") {
                gameVm.fetchAllowedDoubleMoves(gameId, username)
            }
        }
    }

    Scaffold { padding ->
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.drawable.background1),
            contentDescription = "background",
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .padding(padding)
        ) {


           
            BottomBar(
                gameVm = gameVm,
                username = username,
                gameId = gameId,
                isMyTurn = isMyTurn,
                showMrXHistory = showMrXHistory,
                onToggleHistory = {
                    if (!showMrXHistory) {
                        gameVm.fetchMrXHistory(gameId) { history ->
                            mrXHistory = history
                        }
                    }
                    showMrXHistory = !showMrXHistory
                },
                onUpdateHistory = { newHistory ->
                    mrXHistory = newHistory
                }
            )



            val ticketImage = when (visibleTicket?.uppercase()) {
                "TAXI" -> R.drawable.ticket_taxi
                "BUS" -> R.drawable.ticket_bus
                "UNDERGROUND" -> R.drawable.ticket_under
                "BLACK" -> R.drawable.ticket_black
                "DOUBLE" -> R.drawable.ticket_double
                else -> null
            }

            //verwendetest Ticket anzeigen
            AnimatedVisibility(
                visible = !visibleTicket.isNullOrBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (!visibleTicket.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ticketImage?.let {
                                Image(
                                    painter = painterResource(id = it),
                                    contentDescription = visibleTicket,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(
                                text = "Ticket verwendet: ${visibleTicket?.lowercase()?.replaceFirstChar { it.uppercase() }}",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            //MrX history
            AnimatedVisibility(visible = showMrXHistory) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = { showMrXHistory = false }) // Klick außerhalb =schließen
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    // clicks auf dem overlay schließen das overlay nicht
                    Box(
                        modifier = Modifier
                            .widthIn(min = 240.dp, max = 320.dp)
                            .fillMaxHeight()
                            .background(Color.Transparent) // wichtig!
                            .clickable(enabled = false) {}, // blockiert Klickweitergabe
                    ) {
                        Card(
                            modifier = Modifier.fillMaxSize(),
                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.85f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = "MrX Verlauf",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Spacer(Modifier.height(12.dp))

                                mrXHistory.forEach { entry ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .padding(vertical = 4.dp)
                                            .background(
                                                color = Color.White.copy(alpha = 0.05f),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        val round = entry.substringBefore(":")
                                        val details = entry.substringAfter(":").trim()
                                        val position = details.substringBefore(" ")
                                        val ticket = details.substringAfter("(").substringBefore(")")

                                        if (position != "?") {
                                            // Reveal-Runde: Zahl + Ticket daneben
                                            Text(
                                                text = "$round: $position",
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            TicketImage(ticket)
                                        } else {
                                            // Nicht-Reveal: Ticket ersetzt Position
                                            Text(
                                                text = "$round:",
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            TicketImage(ticket)
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }





            Map(gameVm, useSmallMap, allowedMoves,gameId,username,playerPositions,isMyTurn,userSessionVm,mrXPosition)
            
            
                

            //TODO show last MrX Position when revealed
            Box(modifier = Modifier
                .padding(2.dp)
                .align(Alignment.TopStart)
                .background(color = colorResource(R.color.buttonBlue).copy(alpha = 0.5f))
            ){
                Column {
                    Text(modifier = Modifier.padding(8.dp), text = "Rolle: ${userSessionVm.role.value}", color = Color.White)

                    //Placeholder Text
                    // TODO replace Text with showing Players in Board
                    Text("Spielerpositionen:", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Spacer(Modifier.height(8.dp))

                    if (userSessionVm.role.value == "MRX") {
                        mrXPosition?.let {
                            Text("MrX steht auf: $it", color = Color.White)
                        }
                        Spacer(Modifier.height(16.dp))
                        gameUpdate?.playerPositions?.forEach { (name, pos) ->
                            Text("$name steht auf Feld $pos", color = Color.White)
                        }
                    } else {
                        gameUpdate?.playerPositions?.forEach { (name, pos) ->
                            Text("$name steht auf Feld $pos", color = Color.White)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    val myPosition = gameUpdate?.playerPositions?.get(username)
                    myPosition?.let {
                        Spacer(Modifier.height(16.dp))
                        Text("➡ Du stehst auf Feld $it", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Spacer(Modifier.height(16.dp))
                    }                }
            }
        }
    }
}

@Composable
private fun BoxScope.BottomBar(
    gameVm: GameViewModel,
    username: String?,
    gameId: String,
    isMyTurn: Boolean,
    showMrXHistory: Boolean,
    onToggleHistory: () -> Unit,
    onUpdateHistory: (List<String>) -> Unit
)
 {
    Row(modifier = Modifier.align(Alignment.BottomCenter)) {
        //Confirm Button
        Button(
            onClick = {
                username?.let { name ->
                    gameVm.move(
                        gameId,
                        name,
                        gameVm.selectedStation,
                        gameVm.selectedTicket.toString()
                    )
                    Thread.sleep(2000L)
                    gameVm.fetchMrXPosition(gameId, username)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.buttonBlue)),
            enabled = isMyTurn
        ) {
            Text("Confirm")
        }

        // Tickets
        val spacermod = Modifier.width(12.dp)
        SelectableDoubleTicket(gameVm = gameVm)
        Spacer(spacermod)
        SelectableTicket(gameVm = gameVm, imageRes = R.drawable.ticket_black, ticket = Ticket.BLACK)
        Spacer(spacermod)
        SelectableTicket(gameVm = gameVm, imageRes = R.drawable.ticket_taxi, ticket = Ticket.TAXI)
        Spacer(spacermod)
        SelectableTicket(gameVm = gameVm, imageRes = R.drawable.ticket_bus, ticket = Ticket.BUS)
        Spacer(spacermod)
        SelectableTicket(
            gameVm = gameVm,
            imageRes = R.drawable.ticket_under,
            ticket = Ticket.UNDERGROUND
        )
        Spacer(spacermod)

        //Zoom
        Button(
            onClick = { gameVm.increaseZoom() },
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.buttonBlue))
        ) {
            Text("+")
        }
        Spacer(spacermod)

        Button(
            onClick = { gameVm.decreaseZoom() },
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.buttonBlue))
        ) {
            Text("-")
        }

        Spacer(modifier = Modifier.width(12.dp))

        Button(
            onClick = {
                println("MrX-Button gedrückt")
                if (!showMrXHistory) {
                    println("MrX-History wird angefragt")
                    gameVm.fetchMrXHistory(gameId) { history ->
                        println("Antwort: $history")
                        onUpdateHistory(history)
                    }
                }
                onToggleHistory()
            },
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.buttonBlue))
        ) {
            Text(if (showMrXHistory) "Verlauf" else "Verlauf")
        }



    }


}

@Composable
fun SelectableTicket(
    gameVm: GameViewModel,
    imageRes: Int,
    ticket: Ticket,
) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .border(
                width = if (gameVm.selectedTicket == ticket) 3.dp else 0.dp,
                color = if (gameVm.selectedTicket == ticket) Color.Blue else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { gameVm.selectedTicket = ticket }
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
        )
    }
}

@Composable
fun SelectableDoubleTicket(
    gameVm: GameViewModel
) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .border(
                width = if (gameVm.isDoubleMoveMode) 3.dp else 0.dp,
                color = if (gameVm.isDoubleMoveMode) Color.Blue else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { gameVm.isDoubleMoveMode = !gameVm.isDoubleMoveMode }
    ) {
        Image(
            painter = painterResource(id = R.drawable.ticket_double),
            contentDescription = null,
        )
    }
}

@Composable
fun Map(
    gameVm: GameViewModel,
    useSmallMap: Boolean,
    allowedMoves: List<AllowedMoveResponse>,
    gameId:String,
    username: String?,
    playerPositions: Map<String, Int>,
    isMyTurn : Boolean,
    userSessionVm: UserSessionViewModel,
    mrXPosition: Int?
) {
    val mapPainter = painterResource(id = if (useSmallMap) R.drawable.map_small else R.drawable.map)
    val intrinsicSize = mapPainter.intrinsicSize
    val points = gameVm.pointPositions

    val scrollStateX = rememberScrollState()
    val scrollStateY = rememberScrollState()

    val density = LocalDensity.current
    val virtualWidthDp = with(density) { (intrinsicSize.width * gameVm.scale).toDp() }
    val virtualHeightDp = with(density) { (intrinsicSize.height * gameVm.scale).toDp() }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    .size(virtualWidthDp, virtualHeightDp)
            ) {
                //INFO: hier kommt alles rein, ws mit der Map Skalieren soll

                Image(
                    painter = mapPainter,
                    contentDescription = "Map",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
                Stations(gameVm, points, density, allowedMoves, gameId,username,isMyTurn)
                PlayerPositions(gameVm,points,density, playerPositions,userSessionVm,mrXPosition)
            }
        }
    }
}

@Composable
private fun Stations(
    gameVm: GameViewModel,
    points: Map<Int, Pair<Int, Int>>,
    density: Density,
    allowedMoves: List<AllowedMoveResponse>,
    gameId: String,
    username: String?,
    isMyTurn : Boolean
) {
    if (!isMyTurn) return
    val buttonSizeDp = (1 * gameVm.scale).dp

    val expandedStates = remember { mutableStateMapOf<Int, Boolean>() }


    points.forEach { (id, pos) ->
        val (xPx, yPx) = pos
        val xDp = with(density) { (xPx * gameVm.scale).toDp() }
        val yDp = with(density) { (yPx * gameVm.scale).toDp() }

        // Filter moves that involve the current station
        val movesForStation = allowedMoves.filter { move ->
            try {
                move.keys.contains(id)
            } catch (e: Exception) {
                println("Error checking moves for station $id: ${e.message}")
                false
            }
        }

        val hasMoves = movesForStation.isNotEmpty()


        Box(
            modifier = Modifier
                .offset(x = xDp - buttonSizeDp / 2, y = yDp - buttonSizeDp / 2)
        ) {
            Button(
                onClick = {
                    gameVm.selectedStation = id
                    expandedStates[id] = hasMoves
                },
                modifier = Modifier
                    .size(buttonSizeDp)
                    .border(
                        width = if (hasMoves) 3.dp else 0.dp,
                        color = if (hasMoves) Color.Blue else Color.Transparent,
                        shape = CircleShape
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (gameVm.selectedStation == id) Color.Magenta else Color.Transparent
                ),
                enabled = hasMoves

            ) {}

            if (hasMoves && (expandedStates[id] ?: false)) {
                DropdownMenu(
                    expanded = true,
                    onDismissRequest = { expandedStates[id] = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    movesForStation.forEach { move ->


                            val (targetStation, ticketType) = when {
                                move.keys.size >= 2 -> {
                                    val target = move.keys.firstOrNull { it != id } ?: -1
                                    val ticket = move.values.firstOrNull() ?: ""
                                    target to ticket
                                }
                                else -> {
                                    move.keys.firstOrNull()?.let { key ->
                                        key to (move[key] ?: "")
                                    } ?: (-1 to "")
                                }
                            }

                            if (targetStation != -1 && ticketType.isNotEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("$ticketType → Station $targetStation") },
                                    onClick = {
                                        username?.let { name ->
                                            gameVm.move(gameId, name, targetStation, ticketType)
                                            expandedStates[id] = false
                                        }
                                    }
                                )
                            }

                    }
                }
            }
        }
    }

}@Composable
private fun PlayerPositions(
    gameVm: GameViewModel,
    points: Map<Int, Pair<Int, Int>>,
    density: Density,
    playerPositions: Map<String, Int>,
    userSessionVm: UserSessionViewModel,
    mrXPosition: Int?
) {
    val iconSizeDp = (30 * gameVm.scale).dp


    val playerIcons = listOf(
        R.drawable.blue,
        R.drawable.green,
        R.drawable.purple,
        R.drawable.red
    )

    fun getIconForPlayer(name: String): Int {
        val hash = name.hashCode().absoluteValue
        return playerIcons[hash % playerIcons.size]
    }

    // Normale Spieler-Icons für alle anzeigen
    playerPositions.forEach { (playerName, positionId) ->
        points[positionId]?.let { (xPx, yPx) ->
            Image(
                painter = painterResource(id = getIconForPlayer(playerName)),
                contentDescription = "Position von $playerName",
                modifier = Modifier
                    .size(iconSizeDp)
                    .offset(
                        x = with(density) { (xPx * gameVm.scale).toDp() } - iconSizeDp / 2,
                        y = with(density) { (yPx * gameVm.scale).toDp() } - iconSizeDp / 2
                    ),
                contentScale = ContentScale.Fit
            )
        }
    }

    // Mr.X Icon nur anzeigen, wenn der aktuelle Spieler Mr.X ist
    if (userSessionVm.role.value == "MRX") {
        mrXPosition?.let { positionId ->
            points[positionId]?.let { (xPx, yPx) ->
                Image(
                    painter = painterResource(id = R.drawable.mrx),
                    contentDescription = "Position von Mr.X",
                    modifier = Modifier
                        .size(iconSizeDp)
                        .offset(
                            x = with(density) { (xPx * gameVm.scale).toDp() } - iconSizeDp / 2,
                            y = with(density) { (yPx * gameVm.scale).toDp() } - iconSizeDp / 2
                        ),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }

}






