package at.aau.serg.websocketbrokerdemo.ui.lobby

import GameViewModel
import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import at.aau.serg.websocketbrokerdemo.viewmodel.LobbyViewModel
import at.aau.serg.websocketbrokerdemo.viewmodel.UserSessionViewModel
import com.example.myapplication.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import at.aau.serg.websocketbrokerdemo.data.model.AllowedMoveResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import at.aau.serg.websocketbrokerdemo.data.model.GameUpdate


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

    var isScrollingToMrX by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var lastMrXPosition by remember { mutableStateOf<Int?>(null) }
    var lastMyPosition by remember { mutableStateOf<Int?>(null) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showMrXSurrenderedOverlay by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }



    val playerPos = remember(gameUpdate, username, userSessionVm.role.value, mrXPosition) {
        if (userSessionVm.role.value == "MRX") {

            val position = mrXPosition ?: gameUpdate?.playerPositions?.get(userSessionVm.getMrXName())
            position?.let { gameVm.pointPositions[it] }
        } else {

            gameUpdate?.playerPositions?.get(username)?.let { gameVm.pointPositions[it] }
        }
    }

    fun scrollToPosition(positionId: Int, scope: CoroutineScope) {
        val point = gameVm.pointPositions[positionId]
        point?.let { (x, y) ->
            val targetX = (x * gameVm.scale).toInt() - (screenWidth / 2)
            val targetY = (y * gameVm.scale).toInt() - (screenHeight / 2)

            scope.launch {
                scrollStateX.animateScrollTo(targetX, animationSpec = tween(durationMillis = 1500))
            }

            scope.launch {
                scrollStateY.animateScrollTo(targetY, animationSpec = tween(durationMillis = 1500))
            }
        }

    }






    LaunchedEffect(myPosition, mrXPosition, gameVm.scale) {

        if (!isInitialized) {
            val positionToFocus =
                if (userSessionVm.role.value == "MRX") mrXPosition ?: myPosition else myPosition

            if (positionToFocus != null) {

                gameVm.scale = 1.2f.coerceIn(0.5f, 3f)


                delay(100)


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
            if (userSessionVm.role.value == "MRX") {
                gameVm.updateDoubleMoveMode(false)
            }
        }
    }

    LaunchedEffect(gameUpdate?.currentPlayer) {
        if (username != null && gameUpdate?.currentPlayer == username) {
            gameVm.fetchAllowedMoves(gameId, username)
            gameVm.fetchMrXPosition(gameId, username)
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
            if (gameUpdate?.currentPlayer != username) {
                gameVm.resetMoveModes()
            }
        }

        val ticket = gameUpdate?.lastTicketUsed
        if (!ticket.isNullOrBlank()) {
            visibleTicket = ticket
            delay(2000L)
            visibleTicket = ""
        }
    }


    LaunchedEffect(Unit) {
        lobbyVm.setSystemMessageHandler { message ->
            Log.d("SYSTEM_HANDLER", "Got system message: '$message'")
            if (message == "mrX") {
                showMrXSurrenderedOverlay = true
            } else {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }


    LaunchedEffect(Unit) {
        while (true) {
            delay(10_000) // alle 30 Sekunden
            val player = userSessionVm.username.value ?: continue
            lobbyVm.sendPingToGame(gameId, player)
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

            Map(gameVm, useSmallMap, allowedMoves,gameId,username,playerPositions,isMyTurn,userSessionVm,mrXPosition,scrollStateX,scrollStateY,gameUpdate)
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
                scrollStateX = scrollStateX,
                scrollStateY = scrollStateY,
                playerPos = playerPos,
                gameId = gameId,
                username = username
            )

            MrXPositionOverlay(
                playerPositions = gameUpdate?.playerPositions ?: emptyMap(),
                userSessionVm = userSessionVm
            )

            val myTickets = gameUpdate?.ticketInventory?.get(username)

            if (myTickets != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                ) {
                    TicketBar(tickets = myTickets)
                }
            }





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
                        .padding(horizontal = 12.dp, vertical = 20.dp)
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
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            showMrXHistory = false
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterEnd
                )
                {
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
                    onDismiss = { navigateToLobby = true  },
                    userSessionVm = userSessionVm,
                    playerPositions = gameUpdate?.playerPositions ?: emptyMap()
                )
            }

            if (showMrXSurrenderedOverlay) {
                MrXSurrenderedOverlay(
                    onDismiss = {
                        showMrXSurrenderedOverlay = false
                        navigateToLobby = true
                    }
                )
            }


            Box(modifier = Modifier
                .padding(2.dp)
                .align(Alignment.TopStart)
                .background(color = colorResource(R.color.buttonBlue).copy(alpha = 0.5f))
            ){
                Column {
                    Text(modifier = Modifier.padding(8.dp), text = "Rolle: ${userSessionVm.role.value}", color = Color.White)

                    val currentRound = mrXHistory.lastOrNull()?.substringBefore(":") ?: "Runde 1"

                    Text(modifier = Modifier.padding(8.dp),
                        text = "$currentRound",
                        color = Color.White)
                }
            }

            val currentPlayer = gameUpdate?.currentPlayer

            currentPlayer?.let { playerName ->
                val avatarRes = if (userSessionVm.isMrX(playerName)) R.drawable.mrx
                else userSessionVm.getAvatarDrawableRes(playerName)

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 16.dp)
                        .size(56.dp)
                        .background(Color.Black.copy(alpha = 0.6f), shape = CircleShape),
                    contentAlignment = Alignment.Center


                ) {
                    Image(
                        painter = painterResource(id = avatarRes),
                        contentDescription = "Aktueller Spieler: $playerName",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Box(modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 16.dp)) {

                IconButton(onClick = { showSettingsMenu = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        contentDescription = "Einstellungen",
                    )
                }

                DropdownMenu(
                    expanded = showSettingsMenu,
                    onDismissRequest = { showSettingsMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Spiel verlassen") },
                        onClick = {
                            showSettingsMenu = false
                            val player = userSessionVm.username.value

                            if (player != null) {
                                gameVm.leaveGame(gameId, player) {
                                    Log.d("STOMP", "Leave über Menü")
                                    CoroutineScope(Dispatchers.Main).launch {
                                        delay(1)
                                        navigateToLobby = true
                                    }
                                }
                            } else {
                                navigateToLobby = true
                            }
                        }
                    )
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
    playerPos: Pair<Int, Int>?,
    gameId: String,
    username: String?

) {
    val context = LocalContext.current
    val screenWidth = remember { context.resources.displayMetrics.widthPixels }
    val screenHeight = remember { context.resources.displayMetrics.heightPixels }

    fun adjustZoom(newScale: Float) {
        playerPos?.let { (x, y) ->

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
        if (userSessionVm.role.value == "MRX") {
            ExpandableTicketStackAnimated(
                gameVm = gameVm,
                onBlackClick = {
                    gameVm.isBlackMoveMode = !gameVm.isBlackMoveMode
                },
                onDoubleClick = {
                    gameVm.isDoubleMoveMode = !gameVm.isDoubleMoveMode
                    val name = username ?: return@ExpandableTicketStackAnimated
                    if (gameVm.isDoubleMoveMode) {
                        gameVm.fetchAllowedDoubleMoves(gameId, name)
                    } else {
                        gameVm.fetchAllowedMoves(gameId, name)
                    }
                }
            )


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
    scrollStateY: ScrollState,
    gameUpdate: GameUpdate?
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
                PlayerPositions(gameVm,points,density, playerPositions,userSessionVm,mrXPosition,gameUpdate)
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

    val doubleMoves by remember { derivedStateOf { gameVm.allowedDoubleMoves } }
    val movesToShow = if (gameVm.isDoubleMoveMode) doubleMoves else allowedMoves

    val expandedStates = remember { mutableStateMapOf<Int, Boolean>() }


    points.forEach { (id, pos) ->
        val (xPx, yPx) = pos
        val xDp = with(density) { (xPx * gameVm.scale).toDp() }
        val yDp = with(density) { (yPx * gameVm.scale).toDp() }

        // Filter moves that involve the current station
        val movesForStation = movesToShow.filter { move ->
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
                    modifier = Modifier.background(Color.Black.copy(alpha=0.85f))
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
                                modifier = Modifier.height(40.dp),
                                text= {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                       
                                        val ticketParts = ticketType.split("+")
                                        val firstTicketBaseType = ticketParts.firstOrNull() ?: ""
                                        val secondTicketBaseType = if (ticketParts.size > 1) ticketParts[1] else ""
                                        val positionPart = if (ticketParts.size > 2 && ticketParts.last().startsWith("POS")) ticketParts.last() else ""


                                        fun getImageResId(baseType: String): Int? {
                                            return when (baseType) {
                                                "BUS" -> R.drawable.ticket_bus
                                                "TAXI" -> R.drawable.ticket_taxi
                                                "UNDERGROUND" -> R.drawable.ticket_under
                                                "BLACK" -> R.drawable.ticket_black
                                                else -> null
                                            }
                                        }

                                        getImageResId(firstTicketBaseType)?.let { resId ->
                                            Image(
                                                painter = painterResource(id = resId),
                                                contentDescription = "$firstTicketBaseType Icon",
                                                modifier = Modifier.size(36.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }

                                        if (secondTicketBaseType.isNotEmpty() && secondTicketBaseType != positionPart && ticketParts.size > 1) {
                                            getImageResId(secondTicketBaseType)?.let { resId ->
                                                Image(
                                                    painter = painterResource(id = resId),
                                                    contentDescription = "$secondTicketBaseType Icon",
                                                    modifier = Modifier.size(36.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                        } else if (firstTicketBaseType.isNotEmpty() && secondTicketBaseType.isEmpty()) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }

                                        Text(
                                            text = buildString {
                                                if (positionPart.isNotEmpty()) {
                                                    append(" ($positionPart)")
                                                }
                                                append(" → Station $targetStation")
                                            },
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Medium,
                                                color = Color.White
                                            )
                                        )
                                    }
                                },
                                onClick = {
                                    username?.let { name ->
                                        when {
                                            gameVm.isBlackMoveMode && gameVm.isDoubleMoveMode -> {
                                                val parts = ticketType.split("+")
                                                val positionPart = if (parts.size > 2) parts.last() else ""
                                                val blackTicket = "BLACK+BLACK" + if (positionPart.isNotEmpty()) "+$positionPart" else ""
                                                gameVm.doubleMove(gameId, name, targetStation, blackTicket)
                                            }
                                            gameVm.isBlackMoveMode -> {
                                                gameVm.blackMove(gameId, name, targetStation, ticketType)
                                            }
                                            gameVm.isDoubleMoveMode -> {
                                                gameVm.doubleMove(gameId, name, targetStation, ticketType)
                                            }
                                            else -> {

                                                gameVm.move(gameId, name, targetStation, ticketType)
                                            }
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
    mrXPosition: Int?,
    gameUpdate: GameUpdate?
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

            val infiniteTransition = rememberInfiniteTransition(label = "pulsing")

            // 1. Animation für Skalierung (Pulsieren)
            val pulse by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (playerName == gameUpdate?.currentPlayer) 1.2f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )

            // 2. Glow-Farbe
            val glowColor = if (playerName == gameUpdate?.currentPlayer) Color.Yellow else Color.Transparent

            Box(
                modifier = Modifier
                    .offset(
                        x = displayX - iconSizeDp / 2,
                        y = displayY - iconSizeDp / 2
                    )
                    .graphicsLayer {
                        scaleX = pulse
                        scaleY = pulse
                        shadowElevation = if (playerName == gameUpdate?.currentPlayer) 12f else 0f
                        shape = CircleShape
                        clip = false
                    }
                    .background(glowColor.copy(alpha = 0.4f), shape = CircleShape)
                    .size(iconSizeDp)
                    .zIndex(1f)
            ) {
                Image(
                    painter = painterResource(id = getIconForPlayer(playerName)),
                    contentDescription = "Position von $playerName",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

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
    onDismiss: () -> Unit,
    userSessionVm: UserSessionViewModel,
    playerPositions: Map<String, Int>
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

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                if (isMrXWinner) {
                    // Nur MrX Icon
                    Image(
                        painter = painterResource(id = R.drawable.mrx),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    // Icons aller Detectives
                    playerPositions.keys
                        .filterNot { userSessionVm.isMrX(it) }
                        .forEach { player ->
                            Image(
                                painter = painterResource(id = userSessionVm.getAvatarDrawableRes(player)),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp)
                                    .padding(horizontal = 8.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                }
            }

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


@Composable
fun TicketBar(tickets: Map<String, Int>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp) // sichtbarer Bereich
            .offset(y = 40.dp) //nach unten verschieben
    ) {
        Row(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.spacedBy((-20).dp) // Überlappung
        ) {
            var z = 0f
            tickets.entries.sortedBy { it.key }.forEach { (ticket, count) ->
                TicketWithCount(
                    ticket = ticket,
                    count = count,
                    modifier = Modifier.zIndex(z++)
                )
            }
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TicketWithCount(
    ticket: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    val ticketRes = when (ticket.uppercase()) {
        "TAXI" -> R.drawable.ticket_taxi
        "BUS" -> R.drawable.ticket_bus
        "UNDERGROUND" -> R.drawable.ticket_under
        "BLACK" -> R.drawable.ticket_black
        "DOUBLE" -> R.drawable.ticket_double
        else -> null
    }

    ticketRes?.let {
        var previousCount by remember { mutableStateOf(count) }
        var animateScale by remember { mutableStateOf(false) }

        val scale by animateFloatAsState(
            targetValue = if (animateScale) 1.2f else 1f,
            animationSpec = tween(durationMillis = 300),
            label = "scaleOnTicketChange"
        )

        // Effekt beim Count-Wechsel triggern
        LaunchedEffect(count) {
            if (count != previousCount) {
                animateScale = true
                previousCount = count
                delay(300)
                animateScale = false
            }
        }

        Box(
            contentAlignment = Alignment.TopEnd,
            modifier = modifier
                .size(width = 72.dp, height = 96.dp)
                .graphicsLayer {
                    rotationZ = -8f
                    scaleX = scale
                    scaleY = scale
                }
        ) {
            Image(
                painter = painterResource(id = it),
                contentDescription = ticket,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            Box(
                modifier = Modifier
                    .offset(x = (-50).dp, y = 15.dp)
                    .background(Color.Black, shape = CircleShape)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                AnimatedContent(
                    targetState = count,
                    transitionSpec = {
                        (slideInVertically { -it } + fadeIn() + scaleIn()) togetherWith
                                (slideOutVertically { it } + fadeOut() + scaleOut())
                    },
                    label = "ticketCount"
                ) { animatedCount ->
                    Text(
                        text = animatedCount.toString(),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}






@Composable
fun ExpandableTicketStackAnimated(
    gameVm: GameViewModel,
    onBlackClick: () -> Unit,
    onDoubleClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    /* ---------- TRANSITION ---------- */
    val transition = updateTransition(expanded, label = "ticket_expand")

    // Position & Rotation
    val blackX  by transition.animateDp(label = "blackX")  { if (it) 0.dp else (-80).dp }
    val blackY by transition.animateDp(label = "blackY") { if (it) 0.dp  else   0.dp }
    val blackRot by transition.animateFloat(label = "blackRot") { if (it) 0f else -15f }

    val doubleX by transition.animateDp(label = "doubleX") { if (it) 100.dp else (-80).dp }
    val doubleY by transition.animateDp(label = "doubleY") { if (it) 0.dp else (-30).dp }
    val doubleRot by transition.animateFloat(label = "doubleRot") { if (it) 0f else -15f }

    Box(modifier = Modifier
        .padding(8.dp)
        .offset(y = (-12).dp)
    ) {
        /* BLACK */
        TicketAnimatedButton(
            ticket = "BLACK",
            offsetX = blackX,
            offsetY = blackY,
            rotation = blackRot,
            selected = gameVm.isBlackMoveMode,
            onClick = {
                if (!expanded) expanded = true else onBlackClick()
            },
            zIndex = if (expanded) 1f else 2f
        )

        /* DOUBLE */
        TicketAnimatedButton(
            ticket = "DOUBLE",
            offsetX = doubleX,
            offsetY = doubleY,
            rotation = doubleRot,
            selected = gameVm.isDoubleMoveMode,
            onClick = {
                if (!expanded) expanded = true else onDoubleClick()
            },
            zIndex = if (expanded) 2f else 1f
        )

        if (expanded) {
            IconButton(
                onClick = {
                    expanded = false
                    gameVm.isBlackMoveMode = false
                    gameVm.isDoubleMoveMode = false},

                modifier = Modifier
                    .offset(x = 200.dp, y = 10.dp)
                    .size(32.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Transparent
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Einklappen",
                    tint = Color.Black
                )
            }
        }


    }
}

/* ----------------------------------------------------- */

@Composable
fun TicketAnimatedButton(
    ticket: String,
    offsetX: Dp,
    offsetY: Dp,
    rotation: Float,
    selected: Boolean,
    onClick: () -> Unit,
    zIndex: Float
) {
    val resId = when (ticket.uppercase()) {
        "BLACK"  -> R.drawable.ticket_black
        "DOUBLE" -> R.drawable.ticket_double
        else -> return
    }

    Box(
        modifier = Modifier
            .offset(x = offsetX, y = offsetY)
            .zIndex(zIndex)
            .size(width = 96.dp, height = 48.dp)
            .graphicsLayer { rotationZ = rotation }
            .border(
                width = if (selected) 3.dp else 0.dp,
                color = if (selected) Color.Blue else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
    ) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = ticket,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
    }
}


@Composable
private fun BoxScope.MrXPositionOverlay(
    playerPositions: Map<String, Int>,
    userSessionVm: UserSessionViewModel
) {
    val mrXName = userSessionVm.getMrXName()
    var currentMrXPosition by remember { mutableStateOf<Int?>(null) }
    var showMrXPosition by remember { mutableStateOf(false) }
    var lastDisplayedPosition by remember { mutableStateOf<Int?>(null) }


    val newMrXPosition = playerPositions[mrXName]


    LaunchedEffect(newMrXPosition) {
        if (newMrXPosition != null && newMrXPosition != -1 && newMrXPosition != lastDisplayedPosition) {
            currentMrXPosition = newMrXPosition
            showMrXPosition = true
            lastDisplayedPosition = newMrXPosition


            delay(2000L)
            showMrXPosition = false
        }
    }


    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    AnimatedVisibility(
        visible = showMrXPosition,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = Modifier.align(Alignment.Center)
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = pulse
                    scaleY = pulse
                }
                .background(Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(12.dp))
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.mrx),
                    contentDescription = "Mr. X",
                    modifier = Modifier.size(32.dp)
                )

                Text(
                    text = "Mr. X ist an Station $currentMrXPosition aufgetaucht!",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}