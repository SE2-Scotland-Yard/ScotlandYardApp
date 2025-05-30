package at.aau.serg.websocketbrokerdemo.ui.lobby

import GameViewModel
import android.app.Activity
import android.graphics.Color.alpha
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import at.aau.serg.websocketbrokerdemo.data.model.AllowedMoveResponse
import at.aau.serg.websocketbrokerdemo.viewmodel.Ticket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    val allowedMoves by remember { derivedStateOf { gameVm.allowedMoves } }
    val mrXPosition by remember { derivedStateOf { gameVm.mrXPosition } }
    var showWinnerOverlay by remember { mutableStateOf(false) }


    val playerPositions: Map<String, Int> = gameUpdate?.playerPositions ?: emptyMap()
    val winner = gameUpdate?.winner

    var navigateToLobby by remember { mutableStateOf(false) }

    var showMrXHistory by remember { mutableStateOf(false) }
    var mrXHistory by remember { mutableStateOf<List<String>>(emptyList()) }
    var visibleTicket by remember { mutableStateOf<String?>(null) }
    var previousPlayerPositions by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    var isInitialized by remember { mutableStateOf(false) }
    val myPosition = gameUpdate?.playerPositions?.get(username)

    val scrollStateX = rememberScrollState()
    val scrollStateY = rememberScrollState()

    val context = LocalContext.current
    val screenWidth = remember { context.resources.displayMetrics.widthPixels }
    val screenHeight = remember { context.resources.displayMetrics.heightPixels }

    val isMyTurn = username == gameUpdate?.currentPlayer


    val playerPos = remember(gameUpdate, username, userSessionVm.role.value, mrXPosition) {
        if (userSessionVm.role.value == "MRX") {
            // For Mr. X, prioritize the dedicated mrXPosition if available
            val position = mrXPosition ?: gameUpdate?.playerPositions?.get(userSessionVm.getMrXName())
            position?.let { gameVm.pointPositions[it] }
        } else {
            // For detectives, use their position from playerPositions
            gameUpdate?.playerPositions?.get(username)?.let { gameVm.pointPositions[it] }
        }
    }

    LaunchedEffect(myPosition, mrXPosition, gameVm.scale) {

        if (!isInitialized) {
            val positionToFocus =
                if (userSessionVm.role.value == "MRX") mrXPosition ?: myPosition else myPosition

            if (positionToFocus != null) {
                // Leicht reinzoomen (z.B. auf 1.2f)
                gameVm.scale = 1.2f.coerceIn(0.5f, 3f)

                // Verzögerung für die Animation
                delay(100)

                // Zur eigenen Position scrollen (zentriert)
                val point = gameVm.pointPositions[positionToFocus]
                point?.let { (x, y) ->
                    val targetX = (x * gameVm.scale).toInt() - (screenWidth / 2)
                    val targetY = (y * gameVm.scale).toInt() - (screenHeight / 2)

                    scrollStateX.scrollTo(targetX)
                    scrollStateY.scrollTo(targetY)
                }

                isInitialized = true
            }
        }
    }

    LaunchedEffect(playerPositions) {
        previousPlayerPositions = playerPositions
    }


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

    LaunchedEffect(navigateToLobby) {
        if (navigateToLobby) {
            username?.let { name ->
                context.startActivity(LobbyActivity.createIntent(context, name))
                if (context is Activity) {
                    context.finish()
                }
            }
        }
    }

    LaunchedEffect(winner) {
        if (winner != "NONE") {
            showWinnerOverlay = true
            delay(60000L)
            navigateToLobby = true
        }
    }

    LaunchedEffect(gameUpdate) {
        if (username != null) {

            gameVm.fetchMrXPosition(gameId, username)
            if (userSessionVm.role.value == "MRX") {
                gameVm.fetchAllowedDoubleMoves(gameId, username)
            }
            if (gameUpdate?.currentPlayer != username) {
                gameVm.resetMoveModes()
            }
        }
    }
    //Verwendetes Ticket für 2 Sek einblenden
    LaunchedEffect(gameUpdate) {
        val ticket = gameUpdate?.lastTicketUsed
        if (!ticket.isNullOrBlank()) {
            visibleTicket = ticket
            delay(2000L)
            visibleTicket = ""
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

            Map(gameVm, useSmallMap, allowedMoves,gameId,username,playerPositions,isMyTurn,userSessionVm,mrXPosition,scrollStateX,scrollStateY)
            BottomBar(
                gameVm = gameVm,
                userSessionVm = userSessionVm,
                showMrXHistory = showMrXHistory,
                onToggleHistory = {
                    if (!showMrXHistory) {
                        gameVm.fetchMrXHistory(gameId) { history ->
                            mrXHistory = history
                        }
                    }
                    showMrXHistory = !showMrXHistory
                },
                scrollStateX = scrollStateX,  // ScrollStates übergeben
                scrollStateY = scrollStateY,
                playerPos = playerPos  // Spielerposition übergeben
            )


            //verwendetes Ticket anzeigen
            AnimatedVisibility(
                visible = !visibleTicket.isNullOrBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        visibleTicket?.let {
                            val ticketImage = when (it.uppercase()) {
                                "TAXI" -> R.drawable.ticket_taxi
                                "BUS" -> R.drawable.ticket_bus
                                "UNDERGROUND" -> R.drawable.ticket_under
                                "BLACK" -> R.drawable.ticket_black
                                "DOUBLE" -> R.drawable.ticket_double
                                else -> null
                            }
                            ticketImage?.let { res ->
                                Image(
                                    painter = painterResource(id = res),
                                    contentDescription = visibleTicket,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                        }
                        Text(
                            text = "Ticket verwendet: ${visibleTicket?.lowercase()?.replaceFirstChar { it.uppercase() }}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            //MrX history anzeigen
            AnimatedVisibility(visible = showMrXHistory) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { showMrXHistory = false }
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(min = 240.dp, max = 320.dp)
                            .fillMaxHeight()
                            .background(Color.Transparent)
                            .clickable(enabled = false) {},
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
                                            // Sichtbare Runde → zeige Position
                                            Text(
                                                text = "$round: $position",
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        } else {
                                            // Verdeckte Runde → ohne Position
                                            Text(
                                                text = "$round:",
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        TicketImage(ticket)

                                    }
                                }
                            }
                        }
                    }
                }
            }



            if (showWinnerOverlay) {
                WinnerOverlay(
                    winner = winner,
                    currentPlayerRole = userSessionVm.role.value,
                    onDismiss = { navigateToLobby = true  }
                )
            }

            Box(modifier = Modifier
                .padding(2.dp)
                .align(Alignment.TopStart)
                .background(color = colorResource(R.color.buttonBlue).copy(alpha = 0.5f))
            ){
                Column {
                    Text(modifier = Modifier.padding(8.dp), text = "Rolle: ${userSessionVm.role.value}", color = Color.White)
             }
            }
        }
    }
}

@Composable
private fun BoxScope.BottomBar(
    gameVm: GameViewModel,
    userSessionVm: UserSessionViewModel,
    showMrXHistory: Boolean,
    onToggleHistory: () -> Unit,
    scrollStateX: ScrollState,
    scrollStateY: ScrollState,
    playerPos: Pair<Int, Int>?

) {
    val context = LocalContext.current
    val screenWidth = remember { context.resources.displayMetrics.widthPixels }
    val screenHeight = remember { context.resources.displayMetrics.heightPixels }

    fun adjustZoom(newScale: Float) {
        playerPos?.let { (x, y) ->
            val oldScale = gameVm.scale
            gameVm.scale = newScale.coerceIn(0.5f, 3f)

            // Scroll-Position berechnen und anpassen
            val targetX = (x * gameVm.scale).toInt() - screenWidth / 2
            val targetY = (y * gameVm.scale).toInt() - screenHeight / 2

            CoroutineScope(Dispatchers.Main).launch {
                scrollStateX.scrollTo(targetX)
                scrollStateY.scrollTo(targetY)
            }
        }
    }



    val spacermod = Modifier.width(12.dp)
    Row(modifier = Modifier.align(Alignment.BottomStart)) {

        Spacer(spacermod)
        // Black Move Mode Button (nur für Mr. X)
        if (userSessionVm.role.value == "MRX") {
            BlackMoveModeButton(gameVm = gameVm)
            Spacer(spacermod)
        }

        // Double Move Button (nur für Mr. X)
        if (userSessionVm.role.value == "MRX") {
            SelectableDoubleTicket(gameVm = gameVm)
            Spacer(spacermod)
        }

    }

    Row(modifier = Modifier.align(Alignment.BottomEnd)) {

        //Zoom
        Button(
            onClick = { adjustZoom(gameVm.scale + 0.1f) },
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.buttonBlue))
        ) {
            Text("+")
        }
        Spacer(spacermod)

        Button(
            onClick = { adjustZoom(gameVm.scale - 0.1f)  },
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.buttonBlue))
        ) {
            Text("-")
        }
        Spacer(spacermod)
        Spacer(spacermod)

        Button(
            onClick = onToggleHistory,
            colors = ButtonDefaults.buttonColors(containerColor = if (showMrXHistory) Color.Gray else colorResource(id = R.color.buttonBlue))
        ) {
            Text(if (showMrXHistory) "Schließen" else "MrX Verlauf")
        }

    }
}

@Composable
fun BlackMoveModeButton(gameVm: GameViewModel) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .border(
                width = if (gameVm.isBlackMoveMode) 3.dp else 0.dp,
                color = if (gameVm.isBlackMoveMode) Color.Blue else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { gameVm.isBlackMoveMode = !gameVm.isBlackMoveMode }
    ) {
        Image(
            painter = painterResource(id = R.drawable.ticket_black),
            contentDescription = "Black Move Mode"
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
    mrXPosition: Int?,
    scrollStateX: ScrollState,
    scrollStateY: ScrollState
) {
    val mapPainter = painterResource(id = if (useSmallMap) R.drawable.map_small else R.drawable.map)
    val intrinsicSize = mapPainter.intrinsicSize
    val points = gameVm.pointPositions

    val density = LocalDensity.current
    val virtualWidthDp = with(density) { (intrinsicSize.width * gameVm.scale).toDp() }
    val virtualHeightDp = with(density) { (intrinsicSize.height * gameVm.scale).toDp() }

    Box(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .horizontalScroll(scrollStateX)
                .verticalScroll(scrollStateY)
                .align(Alignment.Center)
        ) {

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
                                            if (gameVm.isBlackMoveMode) {
                                                gameVm.blackMove(gameId, name, targetStation,ticketType)
                                            } else {
                                                gameVm.move(gameId, name, targetStation, ticketType)
                                            }
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

}
@Composable
private fun PlayerPositions(
    gameVm: GameViewModel,
    points: Map<Int, Pair<Int, Int>>,
    density: Density,
    playerPositions: Map<String, Int>,
    userSessionVm: UserSessionViewModel,
    mrXPosition: Int?
) {
    val iconSizeDp = (40 * gameVm.scale).dp
    var previousPlayerPositions by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var lastPlayerPositions by remember { mutableStateOf<Map<String, Pair<Float, Float>>>(emptyMap()) }
    var lastMrXPosition by remember { mutableStateOf<Pair<Float, Float>?>(null) }

    LaunchedEffect(playerPositions) {
        previousPlayerPositions = playerPositions
    }


    fun getIconForPlayer(name: String): Int {
        return if (userSessionVm.isMrX(name)) {
            R.drawable.mrx
        } else {
            userSessionVm.getAvatarDrawableRes(name)
        }
    }


    playerPositions.forEach { (playerName, positionId) ->
        if (userSessionVm.isMrX(playerName)) return@forEach
        points[positionId]?.let { (xPx, yPx) ->

            val animatedX by animateFloatAsState(
                targetValue = xPx.toFloat(),
                animationSpec = tween(durationMillis = 1000),
                label = "xAnimation_$playerName"
            )

            val animatedY by animateFloatAsState(
                targetValue = yPx.toFloat(),
                animationSpec = tween(durationMillis = 1000),
                label = "yAnimation_$playerName"
            )

            val displayX = with(density) { (animatedX * gameVm.scale).toDp() }
            val displayY = with(density) { (animatedY * gameVm.scale).toDp() }


            Image(
                painter = painterResource(id = getIconForPlayer(playerName)),
                contentDescription = "Position von $playerName",
                modifier = Modifier
                    .size(iconSizeDp)
                    .offset(
                        x = displayX - iconSizeDp / 2,
                        y = displayY - iconSizeDp / 2
                    ),
                contentScale = ContentScale.Fit
            )
            LaunchedEffect(positionId) {
                lastPlayerPositions = lastPlayerPositions + (playerName to (xPx.toFloat() to yPx.toFloat()))
            }
        }
    }

    val mrXPos = playerPositions[userSessionVm.getMrXName()]

    mrXPos?.let { positionId ->
        points[positionId]?.let { (xPx, yPx) ->
            val animatedX by animateFloatAsState(targetValue = xPx.toFloat(), animationSpec = tween(1000))
            val animatedY by animateFloatAsState(targetValue = yPx.toFloat(), animationSpec = tween(1000))

            val displayX = with(density) { (animatedX * gameVm.scale).toDp() }
            val displayY = with(density) { (animatedY * gameVm.scale).toDp() }

            Image(
                painter = painterResource(id = R.drawable.mrx), // Spezielles Mr. X Icon
                contentDescription = "Position von Mr. X",
                modifier = Modifier
                    .size(iconSizeDp)
                    .offset(displayX - (iconSizeDp * 1.2f) / 2, displayY - (iconSizeDp * 1.2f) / 2),
                contentScale = ContentScale.Fit
            )
        }
    }


    if (userSessionVm.role.value == "MRX") {
        mrXPosition?.let { positionId ->
            points[positionId]?.let { (xPx, yPx) ->

                val animatedX by animateFloatAsState(
                    targetValue = xPx.toFloat(),
                    animationSpec = tween(durationMillis = 1000),
                    label = "xAnimation_MrX"
                )

                val animatedY by animateFloatAsState(
                    targetValue = yPx.toFloat(),
                    animationSpec = tween(durationMillis = 1000),
                    label = "yAnimation_MrX"
                )

                val displayX = with(density) { (animatedX * gameVm.scale).toDp() }
                val displayY = with(density) { (animatedY * gameVm.scale).toDp() }

                Image(
                    painter = painterResource(id = R.drawable.mrx),
                    contentDescription = "Position von Mr.X",
                    modifier = Modifier
                        .size(iconSizeDp)
                        .offset(
                            x = displayX - iconSizeDp / 2,
                            y = displayY - iconSizeDp / 2
                        ),
                    contentScale = ContentScale.Fit
                )
                LaunchedEffect(positionId) {
                    lastMrXPosition = xPx.toFloat() to yPx.toFloat()
                }
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
                    id = if (isMrXWinner) R.drawable.mrx else R.drawable.fox
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





