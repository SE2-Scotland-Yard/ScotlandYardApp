package at.aau.serg.websocketbrokerdemo.functions

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object CoordinateLoader {

    private var loadedCoordinates: Map<Int, Coordinate>? = null

    fun load(context: Context): Map<Int, Coordinate> {
        if (loadedCoordinates != null) return loadedCoordinates!!

        val inputStream = context.assets.open("PointPositions.json")
        val json = inputStream.bufferedReader().use { it.readText() }

        val type = object : TypeToken<Map<Int, List<Coordinate>>>() {}.type
        val rawMap: Map<Int, List<Coordinate>> = Gson().fromJson(json, type)

        // extrahiert nur den ersten Koordinatenpunkt je Feld
        loadedCoordinates = rawMap.mapValues { it.value.first() }
        return loadedCoordinates!!
    }

    data class Coordinate(val x: Int, val y: Int)
}