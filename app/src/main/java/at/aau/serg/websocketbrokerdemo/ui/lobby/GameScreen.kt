package at.aau.serg.websocketbrokerdemo.ui.lobby

import GameViewModel
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.media.MediaPlayer

import android.util.Log
import android.widget.Toast
import androidx.annotation.RawRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.media3.common.util.UnstableApi
import at.aau.serg.websocketbrokerdemo.data.model.GameUpdate
import at.aau.serg.websocketbrokerdemo.ui.auth.VideoPlayerComposable
import androidx.compose.ui.unit.IntSize



@androidx.annotation.OptIn(UnstableApi::class)
@SuppressLint("UnrememberedMutableState")
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
    val winner: String = gameUpdate?.winner ?: "NONE"
    var navigateToLobby by remember { mutableStateOf(false) }

    var showMrXHistory by remember { mutableStateOf(false) }
    var mrXHistory by remember { mutableStateOf<List<String>>(emptyList()) }
    var visibleTicket by remember { mutableStateOf<String?>(null) }


    val myPosition = gameUpdate?.playerPositions?.get(username)
    val context = LocalContext.current


    val isMyTurn by derivedStateOf {
        username == gameUpdate?.currentPlayer
    }


    val coroutineScope = rememberCoroutineScope()

    var showMrXSurrenderedOverlay by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }

    var showCheatArrow by remember { mutableStateOf(false) }
    var mrxXY by remember { mutableStateOf(Pair(0f, 0f)) }
    var hasUsedCheat by remember { mutableStateOf(false) }
    val showCheatHint = remember { mutableStateOf(false) }

    var showLoadingOverlay by remember { mutableStateOf(true) }


    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Activity.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val shakeDetector = ShakeDetector {
            Log.d("SHAKE", "Shake erkannt!")

            if (isMyTurn && username != "MrX" && myPosition != null  && !hasUsedCheat) {
                Log.d("SHAKE", "isMyTurn = $isMyTurn, username = $username, myPosition = $myPosition")

                hasUsedCheat = true

                coroutineScope.launch {
                    val mrXName = userSessionVm.getMrXName()
                    Log.d("SHAKE", "MrX Name: $mrXName")

                    mrXName?.let {
                        gameVm.fetchMrXPosition(gameId, it)
                    }

                    delay(200)

                    val pointPositions = gameVm.repository.getPointPositions(context)
                    val myXYRaw = pointPositions[myPosition] ?: Pair(0, 0)
                    Log.d("SHAKE", "myXYRaw = $myXYRaw")

                    val mrXStationId = gameVm.mrXPosition
                    Log.d("SHAKE", "MrX Station ID: $mrXStationId")

                    val mrxXYRaw = pointPositions[mrXStationId] ?: Pair(0, 0)
                    Log.d("SHAKE", "mrxXYRaw = $mrxXYRaw")

                    mrxXY = Pair(mrxXYRaw.first.toFloat(), mrxXYRaw.second.toFloat())
                    Log.d("SHAKE", "mrxXY gesetzt auf: $mrxXY")

                    showCheatArrow = true
                    Log.d("SHAKE", "showCheatArrow = true")
                }
            } else {
                Log.d("SHAKE", "Shake ignoriert: isMyTurn=$isMyTurn, username=$username, myPosition=$myPosition")
            }
        }

        sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI)

        onDispose {
            sensorManager.unregisterListener(shakeDetector)
        }
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

            gameVm.fetchMrXHistory(gameId) { history ->
                mrXHistory = history
            }

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

    LaunchedEffect(isMyTurn) {
        if (!isMyTurn && showCheatArrow) {
            showCheatArrow = false
            Log.d("SHAKE", "Zug vorbei – Cheat-Pfeil ausgeblendet")
        }
    }







    Scaffold { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Hintergrundbild
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(R.drawable.background3),
                contentDescription = "background",
                contentScale = ContentScale.Crop
            )


            val screenWidth = LocalConfiguration.current.screenWidthDp.dp

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomStart
            ) {
                Image(
                    painter = painterResource(R.drawable.avatarbottombar),
                    contentDescription = "avatar detective",
                    modifier = Modifier
                        .padding(start = screenWidth * 0.1f)
                        .size(100.dp)
                )
            }
        }

        // Haupt-UI mit Map etc.
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Map(
                gameVm = gameVm,
                useSmallMap = useSmallMap,
                allowedMoves = allowedMoves,
                gameId = gameId,
                username = username,
                playerPositions = playerPositions,
                isMyTurn = isMyTurn,
                userSessionVm = userSessionVm,
                mrXPosition = mrXPosition,
                gameUpdate = gameUpdate,
                showCheatArrow = showCheatArrow,
                mrxXY = mrxXY
            )

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
                        .padding(top=60.dp,end = 16.dp)
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

                                mrXHistory.reversed().forEach { entry ->
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
                        text = currentRound,
                        color = Color.White)
                }
                //cheat hint, wird bei klick weider ausgeblendet
                if (showCheatHint.value) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(3f)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                showCheatHint.value = false
                            },
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(16.dp)
                                .background(Color.Black.copy(alpha = 0.75f), shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .clickable(enabled = false) {}
                        ) {
                            Column {
                                Text(
                                    text = "Letzte Energie?",
                                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Dein Ortungsgerät flackert – die Batterie schafft vielleicht noch einen letzten Ping.\n" +
                                            "Ein kräftiger Ruck kann es nochmal zum Leben erwecken.\n" +
                                            "Aber nur während deines Zugs – und nur ein einziges Mal!",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                                )
                            }
                        }
                    }
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
                    if (userSessionVm.role.value != "MRX") {
                        DropdownMenuItem(
                            text = { Text("Ortungsgerät prüfen") },
                            onClick = {
                                showSettingsMenu = false
                                showCheatHint.value = true
                            }
                        )
                    }

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



        AnimatedVisibility(
            visible = showLoadingOverlay,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(100f)
        ){
            Box(
                modifier = Modifier
                    .fillMaxSize(), contentAlignment = Alignment.Center

            ) {
                VideoPlayerComposable(
                    videoUri = "file:///android_asset/loadingScreen.mp4",  // WICHTIG: MIT .mp4-Endung!
                    modifier = Modifier.fillMaxWidth(),
                    looping = false,
                    onVideoEnd = {
                        showLoadingOverlay = false
                    }

                )
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
    gameId: String,
    username: String?

) {

    fun adjustZoom(newScale: Float) {

            gameVm.scale = newScale.coerceIn(0.5f, 3f)


            CoroutineScope(Dispatchers.Main).launch {

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
    gameId: String,
    username: String?,
    playerPositions: Map<String, Int>,
    isMyTurn: Boolean,
    userSessionVm: UserSessionViewModel,
    mrXPosition: Int?,
    gameUpdate: GameUpdate?,
    mrxXY: Pair<Float, Float>,
    showCheatArrow: Boolean
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val mapPainter   = painterResource(if (useSmallMap) R.drawable.map_small else R.drawable.map)
    val tablePainter = painterResource(R.drawable.table)

    val mapWidthPx   = mapPainter.intrinsicSize.width
    val mapHeightPx  = mapPainter.intrinsicSize.height
    val aspectRatio  = mapWidthPx / mapHeightPx
    val borderDp     = 64.dp

    var screenSize by remember { mutableStateOf(IntSize.Zero) }
    var boardSize by remember { mutableStateOf(IntSize.Zero) }

    val minScale = 1f
    val maxScale = 4f

    val transformState = rememberTransformableState { zoom, pan, _ ->
        val newScale = (gameVm.scale * zoom).coerceIn(minScale, maxScale)

        if (screenSize.width > 0 && screenSize.height > 0) {
            val sw = screenSize.width.toFloat()
            val sh = screenSize.height.toFloat()
            val scaledW = sw * newScale
            val scaledH = sh * newScale
            val maxX = ((scaledW - sw) / 2f).coerceAtLeast(0f)
            val maxY = ((scaledH - sh) / 2f).coerceAtLeast(0f)

            gameVm.offsetX = (gameVm.offsetX + pan.x).coerceIn(-maxX, maxX)
            gameVm.offsetY = (gameVm.offsetY + pan.y).coerceIn(-maxY, maxY)
        }

        gameVm.scale = newScale
    }

    // START: Added for zoom-to-MrX feature
    val coroutineScope = rememberCoroutineScope()
    var lastZoomedMrXPosition by remember { mutableStateOf<Int?>(null) }

    val mrXName = userSessionVm.getMrXName()
    val currentMrXPosition = playerPositions[mrXName]

    LaunchedEffect(currentMrXPosition, userSessionVm.role.value, boardSize) {
        // Condition: Only trigger for detectives when Mr. X appears at a new station
        if (
            userSessionVm.role.value != "MRX" &&
            currentMrXPosition != null &&
            currentMrXPosition != -1 &&
            currentMrXPosition != lastZoomedMrXPosition &&
            boardSize != IntSize.Zero // Ensure map is measured
        ) {
            lastZoomedMrXPosition = currentMrXPosition
            val targetStationInfo = gameVm.pointPositions[currentMrXPosition] ?: return@LaunchedEffect

            // 1. Save the user's current camera settings
            val previousScale = gameVm.scale
            val previousOffsetX = gameVm.offsetX
            val previousOffsetY = gameVm.offsetY

            // 2. Define the zoom-in target
            val targetScale = 2.5f

            // 3. Calculate the required offset to center the station on the screen
            val scaleX = boardSize.width.toFloat() / mapWidthPx
            val scaleY = boardSize.height.toFloat() / mapHeightPx
            val targetBoardX = targetStationInfo.first * scaleX
            val targetBoardY = targetStationInfo.second * scaleY

            // Vector from the center of the board to the target station
            val vecX = targetBoardX - (boardSize.width / 2f)
            val vecY = targetBoardY - (boardSize.height / 2f)

            // To center the target, we apply an opposite translation, scaled by the new zoom level
            val targetOffsetX = -vecX * targetScale
            val targetOffsetY = -vecY * targetScale


            // 4. Animate TO the new position
            coroutineScope.launch {
                launch { Animatable(previousScale).animateTo(targetScale, tween(1000)) { gameVm.scale = value } }
                launch { Animatable(previousOffsetX).animateTo(targetOffsetX, tween(1000)) { gameVm.offsetX = value } }
                launch { Animatable(previousOffsetY).animateTo(targetOffsetY, tween(1000)) { gameVm.offsetY = value } }
            }

            // 5. Wait, then animate BACK to the original position
            delay(2500L)

            coroutineScope.launch {
                launch { Animatable(gameVm.scale).animateTo(previousScale, tween(1000)) { gameVm.scale = value } }
                launch { Animatable(gameVm.offsetX).animateTo(previousOffsetX, tween(1000)) { gameVm.offsetX = value } }
                launch { Animatable(gameVm.offsetY).animateTo(previousOffsetY, tween(1000)) { gameVm.offsetY = value } }
            }
        }
    }



    Box(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned { screenSize = it.size }
            .transformable(state = transformState)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX       = gameVm.scale
                    scaleY       = gameVm.scale
                    translationX = gameVm.offsetX
                    translationY = gameVm.offsetY
                }
        ) {
            // Hintergrund: Tisch
            Image(
                painter = tablePainter,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )

            // Brett + Overlays
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(borderDp)
            ) {
                Box(
                    Modifier
                        .align(Alignment.Center)
                        .aspectRatio(aspectRatio)
                        .onGloballyPositioned { boardSize = it.size }
                ) {
                    // Brett
                    Image(
                        painter = mapPainter,
                        contentDescription = "Map",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )


                    val scaleX = boardSize.width.toFloat() / mapWidthPx
                    val scaleY = boardSize.height.toFloat() / mapHeightPx

                    Stations(
                        gameVm       = gameVm,
                        points       = gameVm.pointPositions,
                        density      = density,
                        allowedMoves = allowedMoves,
                        gameId       = gameId,
                        username     = username,
                        isMyTurn     = isMyTurn,
                        scaleX       = scaleX,
                        scaleY       = scaleY
                    )

                    PlayerPositions(
                        gameVm          = gameVm,
                        points          = gameVm.pointPositions,
                        density         = density,
                        playerPositions = playerPositions,
                        userSessionVm   = userSessionVm,
                        mrXPosition     = mrXPosition,
                        gameUpdate      = gameUpdate,
                        scaleMapX       = scaleX,
                        scaleMapY       = scaleY
                    )

                    if (showCheatArrow && username != null) {
                        val myPos = gameUpdate?.playerPositions?.get(username)
                        val pointPositions = gameVm.repository.getPointPositions(context)
                        val myXYRaw = myPos?.let { pointPositions[it] } ?: (0 to 0)
                        val myXY = myXYRaw.first.toFloat() to myXYRaw.second.toFloat()

                        CheatArrow(
                            myXY   = myXY,
                            mrxXY  = mrxXY,
                            scaleX = scaleX,
                            scaleY = scaleY
                        )
                    }
                }
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
    isMyTurn: Boolean,
    scaleX: Float,
    scaleY: Float
) {
    if (!isMyTurn) return
    val buttonSizeDp = 24.dp
    val borderWidthDp = 3.dp

    val doubleMoves by remember { derivedStateOf { gameVm.allowedDoubleMoves } }
    val movesToShow = if (gameVm.isDoubleMoveMode) doubleMoves else allowedMoves
    val expandedStates = remember { mutableStateMapOf<Int, Boolean>() }

    points.forEach { (id, pos) ->
        val (xPx, yPx) = pos
        val xDp = with(density) { (xPx * scaleX).toDp() }
        val yDp = with(density) { (yPx * scaleY).toDp() }

        val movesForStation = movesToShow.filter { it.keys.contains(id) }
        val hasMoves = movesForStation.isNotEmpty()

        Box(
            modifier = Modifier
                .offset(x = xDp - buttonSizeDp / 2, y = yDp - buttonSizeDp / 2)
                .size(buttonSizeDp)
        ) {

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(
                        width = if (hasMoves) borderWidthDp else 0.dp,
                        color = if (hasMoves) Color.Blue else Color.Transparent,
                        shape = CircleShape
                    )
                    .background(
                        color = if (gameVm.selectedStation == id)
                            Color(0xFF448AFF).copy(alpha = 0.25f)
                        else
                            Color.Transparent,
                        shape = CircleShape
                    )
                    .zIndex(1f)
            )


            if (hasMoves) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .zIndex(100f)
                        .pointerInput(id) {
                            detectTapGestures(
                                onTap = {
                                    gameVm.selectedStation = id
                                    expandedStates[id] = true
                                },
                                onDoubleTap = {
                                    gameVm.selectedStation = id
                                    expandedStates[id] = true
                                }
                            )
                        }
                )
            }

            if (hasMoves && expandedStates[id] == true) {
                DropdownMenu(
                    expanded = true,
                    onDismissRequest = { expandedStates[id] = false },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .background(Color.Black.copy(alpha = 0.85f))
                ) {
                    movesForStation.forEach { move ->
                        val (targetStation, ticketType) = when {
                            move.keys.size >= 2 -> {
                                val target = move.keys.first { it != id }
                                val ticket = move.values.first()
                                target to ticket
                            }

                            else -> move.keys.first() to (move[move.keys.first()] ?: "")
                        }

                        if (targetStation != -1 && ticketType.isNotEmpty()) {
                            DropdownMenuItem(
                                modifier = Modifier.height(40.dp),
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        val ticketParts = ticketType.split("+")
                                        val firstTicket = ticketParts.firstOrNull() ?: ""
                                        val secondTicket =
                                            if (ticketParts.size > 1) ticketParts[1] else ""
                                        val positionPart =
                                            ticketParts.lastOrNull()?.takeIf { it.startsWith("POS") } ?: ""

                                        fun imgRes(base: String) = when (base) {
                                            "BUS" -> R.drawable.ticket_bus
                                            "TAXI" -> R.drawable.ticket_taxi
                                            "UNDERGROUND" -> R.drawable.ticket_under
                                            "BLACK" -> R.drawable.ticket_black
                                            else -> null
                                        }

                                        imgRes(firstTicket)?.let { res ->
                                            Image(
                                                painter = painterResource(res),
                                                contentDescription = "$firstTicket-Icon",
                                                modifier = Modifier.size(36.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                        }

                                        if (secondTicket.isNotEmpty()
                                            && secondTicket != positionPart
                                            && ticketParts.size > 1
                                        ) {
                                            imgRes(secondTicket)?.let { res ->
                                                Image(
                                                    painter = painterResource(res),
                                                    contentDescription = "$secondTicket-Icon",
                                                    modifier = Modifier.size(36.dp)
                                                )
                                            }
                                            Spacer(Modifier.width(8.dp))
                                        } else if (firstTicket.isNotEmpty()
                                            && secondTicket.isEmpty()
                                        ) {
                                            Spacer(Modifier.width(8.dp))
                                        }

                                        Text(
                                            text = buildString {
                                                if (positionPart.isNotEmpty()) append(" ($positionPart)")
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
                                                val positionPart =
                                                    if (parts.size > 2) parts.last() else ""
                                                val blackTicket =
                                                    "BLACK+BLACK" + if (positionPart.isNotEmpty()) "+$positionPart" else ""
                                                gameVm.doubleMove(
                                                    gameId,
                                                    name,
                                                    targetStation,
                                                    blackTicket
                                                )
                                            }

                                            gameVm.isBlackMoveMode -> {
                                                gameVm.blackMove(
                                                    gameId,
                                                    name,
                                                    targetStation,
                                                    ticketType
                                                )
                                            }

                                            gameVm.isDoubleMoveMode -> {
                                                gameVm.doubleMove(
                                                    gameId,
                                                    name,
                                                    targetStation,
                                                    ticketType
                                                )
                                            }

                                            else -> {
                                                gameVm.move(
                                                    gameId,
                                                    name,
                                                    targetStation,
                                                    ticketType
                                                )
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


@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
private fun PlayerPositions(
    gameVm: GameViewModel,
    points: Map<Int, Pair<Int, Int>>,
    density: Density,
    playerPositions: Map<String, Int>,
    userSessionVm: UserSessionViewModel,
    mrXPosition: Int?,
    gameUpdate: GameUpdate?,
    scaleMapX: Float,
    scaleMapY: Float
) {
    val iconSizeDp = (60f / gameVm.scale).dp.coerceIn(16.dp, 40.dp)


    var lastPlayerPositions by remember { mutableStateOf<Map<String, Pair<Float, Float>>>(emptyMap()) }
    var previousMrXShadowPosition by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val nudged = remember { mutableStateMapOf<String, Boolean>() }



    fun playSound(context: Context, @RawRes soundResId: Int) {
        try {
            val mediaPlayer = MediaPlayer.create(context, soundResId)
            mediaPlayer?.apply {
                setOnCompletionListener { mp -> mp.release() }
                setOnCompletionListener { mp -> mp.release() }
                start()
            }
        } catch (e: Exception) {
            Log.e("SoundPlayback", "Fehler beim Abspielen des Sounds: ${e.message}")
        }
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
                animationSpec = tween(1000),
                label = "x_$playerName"
            )
            val animatedY by animateFloatAsState(
                targetValue = yPx.toFloat(),
                animationSpec = tween(1000),
                label = "y_$playerName"
            )

            val xDp = with(density) { (animatedX * scaleMapX).toDp() }
            val yDp = with(density) { (animatedY * scaleMapY).toDp() }

            val pulse by rememberInfiniteTransition(label = "pulse_$playerName").animateFloat(
                initialValue = 1f,
                targetValue = if (playerName == gameUpdate?.currentPlayer) 1.2f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )

            val glowColor = if (playerName == gameUpdate?.currentPlayer) Color.Yellow else Color.Transparent
            val nudgeX by animateDpAsState(
                targetValue = if (nudged[playerName] == true) iconSizeDp * .6f else 0.dp,
                animationSpec = tween(300),
                label = "nd_$playerName"
            )

            Box(
                modifier = Modifier
                    .offset(xDp - iconSizeDp / 2 + nudgeX, yDp - iconSizeDp / 2)
                    .graphicsLayer {
                        scaleX = pulse
                        scaleY = pulse
                        shadowElevation = if (playerName == gameUpdate?.currentPlayer) 12f else 0f
                        shape = CircleShape
                        clip = false
                    }
                    .background(glowColor.copy(alpha = 0.4f), CircleShape)
                    .size(iconSizeDp)
                    .then(
                        if (playerName != userSessionVm.username.value) {
                            Modifier.clickable {
                                if (nudged[playerName] != true) {
                                    nudged[playerName] = true
                                    coroutineScope.launch {
                                        delay(2000)
                                        nudged[playerName] = false
                                    }
                                }
                            }
                        } else Modifier
                    )

                    .zIndex(1f)
            ) {
                Image(
                    painter = painterResource(getIconForPlayer(playerName)),
                    contentDescription = "Position von $playerName",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            LaunchedEffect(positionId) {
                lastPlayerPositions = lastPlayerPositions + (playerName to (animatedX to animatedY))
            }
        }
    }

    playerPositions[userSessionVm.getMrXName()]?.let { positionId ->
        LaunchedEffect(positionId) {
            if (previousMrXShadowPosition != null && previousMrXShadowPosition != positionId) {
                playSound(context, R.raw.mrx_appear)
            } else if (previousMrXShadowPosition == null) {
                playSound(context, R.raw.mrx_appear)
            }
            previousMrXShadowPosition = positionId
        }

        points[positionId]?.let { (xPx, yPx) ->
            val animatedX by animateFloatAsState(targetValue = xPx.toFloat(), animationSpec = tween(1000))
            val animatedY by animateFloatAsState(targetValue = yPx.toFloat(), animationSpec = tween(1000))

            val xDp = with(density) { (animatedX * scaleMapX).toDp() }
            val yDp = with(density) { (animatedY * scaleMapY).toDp() }

            val nudgeX by animateDpAsState(
                targetValue = if (nudged["mrX_shadow"] == true) iconSizeDp * .6f else 0.dp,
                animationSpec = tween(300),
                label = "nd_mrX_shadow"
            )

            Box(
                modifier = Modifier
                    .offset(xDp - (iconSizeDp * 1.2f) / 2 + nudgeX, yDp - (iconSizeDp * 1.2f) / 2)
                    .size(iconSizeDp)

            ) {
                Image(
                    painter = painterResource(R.drawable.mrx_shadow),
                    contentDescription = "Position von Mr. X (Schatten)",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }

    if (userSessionVm.role.value == "MRX") {
        mrXPosition?.let { positionId ->
            points[positionId]?.let { (xPx, yPx) ->
                val animatedX by animateFloatAsState(targetValue = xPx.toFloat(), animationSpec = tween(1000))
                val animatedY by animateFloatAsState(targetValue = yPx.toFloat(), animationSpec = tween(1000))

                val xDp = with(density) { (animatedX * scaleMapX).toDp() }
                val yDp = with(density) { (animatedY * scaleMapY).toDp() }

                Image(
                    painter = painterResource(R.drawable.mrx),
                    contentDescription = "Position von Mr.X",
                    modifier = Modifier
                        .size(iconSizeDp)
                        .offset(xDp - iconSizeDp / 2, yDp - iconSizeDp / 2),
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
    onDismiss: () -> Unit,
    userSessionVm: UserSessionViewModel,
    playerPositions: Map<String, Int>
) {
    val isMrXWinner = winner == "MR_X"
    val isCurrentPlayerMrX = currentPlayerRole == "MRX"


    val backgroundColor = getBackgroundColor(isMrXWinner, isCurrentPlayerMrX)

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

private fun getBackgroundColor(isMrXWinner: Boolean, isCurrentPlayerMrX: Boolean): Color {
    return if (isMrXWinner == isCurrentPlayerMrX) Color(0xFF4CAF50) else Color(0xFFF44336)
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


@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun TicketBar(tickets: Map<String, Int>) {

    var anyPressed by remember { mutableStateOf(false) }


    val raise by animateDpAsState(
        targetValue = if (anyPressed) (-20).dp else 0.dp,
        label = "rowRaise"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)     // sichtbarer Bereich
            .offset(y = 40.dp) // Grund-Position weiter unten
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = raise),
            horizontalArrangement = Arrangement.spacedBy((-20).dp)
        ) {
            var z = 0f
            tickets.entries.sortedBy { it.key }.forEach { (ticket, count) ->
                TicketWithCount(
                    ticket       = ticket,
                    count        = count,
                    modifier     = Modifier.zIndex(z++),
                    enlargeAll   = anyPressed,
                    onPressChanged = { pressed ->
                        anyPressed = pressed
                    }
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
    modifier: Modifier = Modifier,
    enlargeAll: Boolean = false,
    onPressChanged: ((Boolean) -> Unit)? = null
) {
    // ───────── Ticket-Grafik wählen ─────────
    val ticketRes = when (ticket.uppercase()) {
        "TAXI"        -> R.drawable.ticket_taxi
        "BUS"         -> R.drawable.ticket_bus
        "UNDERGROUND" -> R.drawable.ticket_under
        "BLACK"       -> R.drawable.ticket_black
        "DOUBLE"      -> R.drawable.ticket_double
        else          -> null
    }

    ticketRes?.let { resId ->

        // ───────── Easter Egg States ─────────
        var clickCount by remember { mutableIntStateOf(0) }
        var lastClickTime by remember { mutableLongStateOf(0L) }
        var eggUnlocked by remember { mutableStateOf(false) }

        // ───────── Responsive Grundgrößen ─────────
        val screenWidthDp = LocalConfiguration.current.screenWidthDp
        val ticketWidth = when {
            screenWidthDp < 400 -> 44.dp
            screenWidthDp < 600 -> 60.dp
            else                -> 80.dp
        }
        val ticketHeight = ticketWidth * (4f / 3f)

        // ───────── Badge-Parameter ─────────
        val badgeOffsetX  = ticketWidth * -0.7f
        val badgeOffsetY  = ticketWidth * 0.05f
        val fontSize      = (ticketWidth.value * 0.2f).sp
        val badgePaddingH = ticketWidth * 0.1f
        val badgePaddingV = ticketWidth * 0.06f

        // ───────── Animations-States ─────────
        var previousCount by remember { mutableIntStateOf(count) }
        var animateScale  by remember { mutableStateOf(false) }
        var isPressed     by remember { mutableStateOf(false) }

        LaunchedEffect(count) {
            if (count != previousCount) {
                animateScale  = true
                previousCount = count
                delay(300)
                animateScale  = false
            }
        }

        val scale by animateFloatAsState(
            targetValue = if (animateScale || isPressed || enlargeAll) 1.25f else 1f,
            animationSpec = tween(durationMillis = 300),
            label = "ticketScale"
        )

        // ───────── UI-Aufbau ─────────
        Box(
            contentAlignment = Alignment.TopEnd,
            modifier = modifier
                .size(ticketWidth, ticketHeight)
                .graphicsLayer {
                    rotationZ = -8f
                    scaleX    = scale
                    scaleY    = scale
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            onPressChanged?.invoke(true)

                            val now = System.currentTimeMillis()
                            if (now - lastClickTime <= 3000) {
                                clickCount++
                                if (clickCount >= 10 && !eggUnlocked) {
                                    eggUnlocked = true
                                    println("Easter Egg unlocked!")

                                }
                            } else {
                                clickCount = 1
                            }
                            lastClickTime = now

                            tryAwaitRelease()
                            isPressed = false
                            onPressChanged?.invoke(false)
                        }
                    )
                }
        ) {
            Image(
                painter = painterResource(id = resId),
                contentDescription = ticket,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            Box(
                modifier = Modifier
                    .offset(badgeOffsetX, badgeOffsetY)
                    .background(Color.Black, CircleShape)
                    .padding(horizontal = badgePaddingH, vertical = badgePaddingV)
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
                        fontSize = fontSize
                    )
                }
            }
        }
    }
}












@SuppressLint("UnusedTransitionTargetStateParameter")
@Composable
fun ExpandableTicketStackAnimated(
    gameVm: GameViewModel,
    onBlackClick: () -> Unit,
    onDoubleClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val baseSize = when {
        screenWidthDp < 360 -> 56.dp  // Sehr kleine Displays
        screenWidthDp < 400 -> 70.dp  // Kleine Displays
        screenWidthDp < 600 -> 90.dp  // Mittelgroß
        else -> 110.dp                // Groß/Tablet
    }

    val ticketWidth = baseSize
    val ticketHeight = baseSize / 2

    val offsetShort = -ticketWidth * 0.8f
    val offsetLong  = ticketWidth * 1.05f

    val transition = updateTransition(expanded, label = "ticket_expand")

    val blackX  by transition.animateDp(label = "blackX")  { if (it) 0.dp else offsetShort }
    val blackY by transition.animateDp(label = "blackY") { 0.dp }

    val blackRot by transition.animateFloat(label = "blackRot") { if (it) 0f else -15f }

    val doubleX by transition.animateDp(label = "doubleX") { if (it) offsetLong else offsetShort }
    val doubleY by transition.animateDp(label = "doubleY") { if (it) 0.dp else (-ticketHeight * 0.6f) }
    val doubleRot by transition.animateFloat(label = "doubleRot") { if (it) 0f else -15f }

    Box(
        modifier = Modifier
            .padding(8.dp)
            .offset(y = (-12).dp)
    ) {
        TicketAnimatedButton(
            ticket = "BLACK",
            offsetX = blackX,
            offsetY = blackY,
            rotation = blackRot,
            selected = gameVm.isBlackMoveMode,
            onClick = {
                if (!expanded) expanded = true else onBlackClick()
            },
            zIndex = if (expanded) 1f else 2f,
            width = ticketWidth,
            height = ticketHeight
        )

        TicketAnimatedButton(
            ticket = "DOUBLE",
            offsetX = doubleX,
            offsetY = doubleY,
            rotation = doubleRot,
            selected = gameVm.isDoubleMoveMode,
            onClick = {
                if (!expanded) expanded = true else onDoubleClick()
            },
            zIndex = if (expanded) 2f else 1f,
            width = ticketWidth,
            height = ticketHeight
        )

        if (expanded) {
            IconButton(
                onClick = {
                    expanded = false
                    gameVm.isBlackMoveMode = false
                    gameVm.isDoubleMoveMode = false
                },
                modifier = Modifier
                    .offset(x = ticketWidth * 2f, y = 10.dp)
                    .size(40.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Transparent
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Einklappen",
                    modifier = Modifier.size(100.dp)
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
    zIndex: Float,
    width: Dp,
    height: Dp
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
            .size(width = width, height = height)
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