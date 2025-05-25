package at.aau.serg.websocketbrokerdemo.ui.lobby

import GameViewModel
import android.graphics.Paint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
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
    val allowedDoubleMoves by remember { derivedStateOf { gameVm.allowedDoubleMoves } }
    val isDoubleMoveMode by remember { derivedStateOf { gameVm.isDoubleMoveMode } }
    val scale by remember { derivedStateOf { gameVm.scale } }

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

            Map(gameVm, useSmallMap, allowedMoves)
            BottomBar(gameVm)
            OverlayLeft(username, userSessionVm, mrXPosition, gameUpdate, message, gameId)
            OverlayRight(error, expanded, isMyTurn, selectedMove, allowedMoves, username, gameVm, gameId )
        }
    }
}

@Composable
private fun BoxScope.BottomBar(gameVm: GameViewModel) {
    Row(
        modifier = Modifier
            .align(Alignment.BottomCenter),
    ) {
        val spacermod = Modifier.width(12.dp)

        SelectableImage(imageRes = R.drawable.ticket_double, isSelected = true) { }

        Spacer(spacermod)

        SelectableImage(imageRes = R.drawable.ticket_black, isSelected = true) {}

        Spacer(spacermod)
        SelectableImage(imageRes = R.drawable.ticket_taxi, isSelected = true) {}

        Spacer(spacermod)
        SelectableImage(imageRes = R.drawable.ticket_bus, isSelected = true) {}

        Spacer(spacermod)
        SelectableImage(imageRes = R.drawable.ticket_under, isSelected = true) {}
        Spacer(spacermod)


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


    }
}

@Composable
fun SelectableImage(
    imageRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) Color.Blue else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
        )
    }
}


@Composable
fun Map(
    gameVm: GameViewModel,
    useSmallMap: Boolean,
    allowedMoves: List<AllowedMoveResponse>,
    ) {
    val mapPainter = painterResource(id = if (useSmallMap) R.drawable.map_small else R.drawable.map)
    val intrinsicSize = mapPainter.intrinsicSize
    val points = gameVm.pointPositions

    val scrollStateX = rememberScrollState()
    val scrollStateY = rememberScrollState()

    val density = LocalDensity.current
    val virtualWidthDp = with(density) { (intrinsicSize.width * gameVm.scale).toDp() }
    val virtualHeightDp = with(density) { (intrinsicSize.height * gameVm.scale).toDp() }

    Box(
        modifier = Modifier
            .fillMaxSize()

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
                    .size(virtualWidthDp, virtualHeightDp)
            ) {
                Image(
                    painter = mapPainter,
                    contentDescription = "Map",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )

                val buttonSizeDp = (15*gameVm.scale).dp

                points.forEach { (id, pos) ->
                    val (xPx, yPx) = pos
                    val xDp = with(density) { (xPx * gameVm.scale).toDp() }
                    val yDp = with(density) { (yPx * gameVm.scale).toDp() }
                    var allowed = false
                    allowedMoves.forEach { move -> if(id in move.keys) allowed = true}

                    Button(
                        onClick = { /* handle click */ },
                        modifier = Modifier
                            .offset(
                                x = xDp - buttonSizeDp / 2,
                                y = yDp - buttonSizeDp / 2
                            )
                            .border(
                                width = if (allowed) 3.dp else 0.dp,
                                color = if (allowed) Color.Blue else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )

                            .size(buttonSizeDp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)

                    ) {
                        // Optional content
                    }
                }
            }
        }
    }
}




