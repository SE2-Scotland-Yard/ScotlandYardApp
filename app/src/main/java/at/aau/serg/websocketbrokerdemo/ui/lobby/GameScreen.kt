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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import at.aau.serg.websocketbrokerdemo.data.model.AllowedMoveResponse
import at.aau.serg.websocketbrokerdemo.viewmodel.Ticket
import kotlinx.coroutines.delay
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
    var showWinnerOverlay by remember { mutableStateOf(false) }

    //TODO import Double move from old Gamescreen
    //States für DoubleMove
    var firstMoveSelected by remember { mutableStateOf<MrXDoubleMoveResponse?>(null) }
    var secondMoveSelected by remember { mutableStateOf<MrXDoubleMoveResponse?>(null) }
    var expandedFirstMove by remember { mutableStateOf(false) }
    var expandedSecondMove by remember { mutableStateOf(false) }
    val playerPositions: Map<String, Int> = gameUpdate?.playerPositions ?: emptyMap()
    val winner = gameUpdate?.winner


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

    LaunchedEffect(winner) {
        if (winner != "NONE") {
            showWinnerOverlay = true
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

            Map(gameVm, useSmallMap, allowedMoves,gameId,username,playerPositions,isMyTurn,userSessionVm,mrXPosition)
            BottomBar(gameVm, username, gameId, isMyTurn,userSessionVm)

            if (showWinnerOverlay) {
                WinnerOverlay(
                    winner = winner,
                    currentPlayerRole = userSessionVm.role.value,
                    onDismiss = { showWinnerOverlay = false }
                )
            }

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

                    gameUpdate?.winner?.let { winner ->
                        Text("Winner: $winner")
                    }

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
    isMyTurn : Boolean,
    userSessionVm: UserSessionViewModel

) {
    val spacermod = Modifier.width(12.dp)
    Row(modifier = Modifier.align(Alignment.BottomStart)) {

        Spacer(spacermod)
        Spacer(spacermod)
        if (userSessionVm.role.value == "MRX") {
            SelectableDoubleTicket(gameVm = gameVm)
            Spacer(spacermod)
            SelectableTicket(
                gameVm = gameVm,
                imageRes = R.drawable.ticket_black,
                ticket = Ticket.BLACK
            )
        }

    }

    Row(modifier = Modifier.align(Alignment.BottomEnd)) {

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
        Spacer(spacermod)
        Spacer(spacermod)
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
@Composable
fun WinnerOverlay(
    winner: String?,
    currentPlayerRole: String?,
    onDismiss: () -> Unit
) {
    val isMrXWinner = winner == "MR_X"
    val isCurrentPlayerMrX = currentPlayerRole == "MRX"


    val backgroundColor = when {
        isMrXWinner && isCurrentPlayerMrX -> Color(0xFF4CAF50)
        !isMrXWinner && !isCurrentPlayerMrX -> Color(0xFF4CAF50)
        else -> Color(0xFFF44336)
    }

    val title = when {
        isMrXWinner && isCurrentPlayerMrX -> "Mr. X hat gewonnen!"
        isMrXWinner && !isCurrentPlayerMrX -> "Mr. X hat gewonnen!"
        !isMrXWinner && isCurrentPlayerMrX -> "Die Detectives haben gewonnen!"
        else -> "Die Detectives haben gewonnen!"
    }


    val message = when {
        isMrXWinner && isCurrentPlayerMrX -> "Glückwunsch! Du hast als Mr. X gewonnen!"
        isMrXWinner && !isCurrentPlayerMrX -> "Mr. X ist entkommen! Versucht es beim nächsten Mal besser!"
        !isMrXWinner && isCurrentPlayerMrX -> "Die Detectives haben dich gefangen!"
        else -> "Glückwunsch! Ihr habt Mr. X gefangen!"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                painter = painterResource(
                    id = if (isMrXWinner) R.drawable.mrx else R.drawable.red
                ),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = backgroundColor
                ),
                modifier = Modifier.width(150.dp)
            ) {
                Text("Zur Lobby")
            }
        }
    }
}




