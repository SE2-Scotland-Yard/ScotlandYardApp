package at.aau.serg.websocketbrokerdemo.ui.lobby

import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset

@Composable
fun ZoomableMapWrapper(
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    onScaleChange: (Float) -> Unit,
    onPanChange: (Offset) -> Unit
) {
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(0.5f, 3f)
        onScaleChange(newScale)
        onPanChange(panChange)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .transformable(state = transformableState)
    )
}