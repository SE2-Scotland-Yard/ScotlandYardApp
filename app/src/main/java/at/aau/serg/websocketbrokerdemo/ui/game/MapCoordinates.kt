package at.aau.serg.websocketbrokerdemo.ui.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

data class MapField(val id: Int, val offset: Offset)

object MapCoordinates {

    val smallMapFields = listOf(
        MapField(1, Offset(100f, 200f)),


    )

    val largeMapFields = listOf(
        MapField(1, Offset(400f, 200f)),


    )
}