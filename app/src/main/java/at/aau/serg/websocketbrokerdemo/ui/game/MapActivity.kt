package at.aau.serg.websocketbrokerdemo.ui.game


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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import at.aau.serg.websocketbrokerdemo.viewmodel.LobbyViewModel
import at.aau.serg.websocketbrokerdemo.viewmodel.UserSessionViewModel
import com.example.myapplication.R


@Composable
fun MapScreen(useSmallMap: Boolean = false) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Row(
        modifier = Modifier.background(color = Color.LightGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Stub for Player Positions, Tickets and some actions")
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { /* TODO: Move Player */ },
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.buttonStartScreen))
            ) {
                Text("Move Player")
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { /* TODO: Use Ticket */ },
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.buttonStartScreen))
            ) {
                Text("Use Ticket")
            }
        }


        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Image(
                painter = painterResource(id = if (useSmallMap) R.drawable.map_small  else R.drawable.map),
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
}
