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
    scale: Float
) {
    val angleRad = atan2(mrxXY.second - myXY.second, mrxXY.first - myXY.first)//Spitze-Schaft
    val angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()
    val offsetDistancePx = 100f //wie weit der Pfeil vom avatar weg ist

    val dx = offsetDistancePx * kotlin.math.cos(angleRad)
    val dy = offsetDistancePx * kotlin.math.sin(angleRad)

    val density = LocalDensity.current
    val xDp = with(density) { ((myXY.first + dx) * scale).toDp() }
    val yDp = with(density) { ((myXY.second + dy) * scale).toDp() }

    val iconSize = 48.dp
    val offsetX = xDp - iconSize / 2
    val offsetY = yDp - iconSize / 2



    Box(
        modifier = Modifier
            .offset(x = offsetX, y = offsetY)
            .size(iconSize)
            .graphicsLayer {
                rotationZ = angleDeg + 90f // +90° weil der Pfeil nach oben zeigt, die Berechnung geht aber davon aus dass der Pfeil nach links schaut
            }
    ) {
        Image(
            painter = painterResource(id = R.drawable.arrow),
            contentDescription = "Pfeil Richtung MrX",
            modifier = Modifier.fillMaxSize()
        )
    }
}
