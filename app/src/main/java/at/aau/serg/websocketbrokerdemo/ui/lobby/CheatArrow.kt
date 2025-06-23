package at.aau.serg.websocketbrokerdemo.ui.lobby


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import kotlin.math.atan2

@Composable
fun CheatArrow(
    myXY: Pair<Float, Float>,
    mrxXY: Pair<Float, Float>,
    scaleX: Float,
    scaleY: Float
) {
    val density = LocalDensity.current

    val dxRaw = mrxXY.first - myXY.first
    val dyRaw = mrxXY.second - myXY.second

    val angleRad = atan2(dyRaw, dxRaw)
    val angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()

    val offsetDistance = 140f
    val dx = offsetDistance * kotlin.math.cos(angleRad)
    val dy = offsetDistance * kotlin.math.sin(angleRad)

    val arrowX = (myXY.first + dx) * scaleX
    val arrowY = (myXY.second + dy) * scaleY

    val iconSize = 48.dp
    val xDp = with(density) { arrowX.toDp() }
    val yDp = with(density) { arrowY.toDp() }

    Box(
        modifier = Modifier
            .offset(x = xDp - iconSize / 2, y = yDp - iconSize / 2)
            .size(iconSize)
            .graphicsLayer {
                rotationZ = angleDeg + 90f
            }
    ) {
        Image(
            painter = painterResource(id = R.drawable.arrow),
            contentDescription = "Pfeil Richtung MrX",
            modifier = Modifier.fillMaxSize()
        )
    }
}



