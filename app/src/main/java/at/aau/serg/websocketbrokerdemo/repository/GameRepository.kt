package at.aau.serg.websocketbrokerdemo.repository

import android.content.Context
import androidx.compose.ui.graphics.vector.EmptyPath
import at.aau.serg.websocketbrokerdemo.data.api.GameApi
import at.aau.serg.websocketbrokerdemo.data.model.AllowedMoveResponse
import at.aau.serg.websocketbrokerdemo.data.model.MoveResponse
import at.aau.serg.websocketbrokerdemo.data.model.MrXDoubleMoveResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.JsonParser
import com.google.gson.Gson


class GameRepository (
    private val api: GameApi = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GameApi::class.java)
    )
    {
        suspend fun move(gameId: String, name: String, to: Int, gotTicket: String): String {
            return api.move(gameId, name, to, gotTicket).message
        }

        suspend fun blackMove(gameId: String, name: String, to: Int, gotTicket: String):String{
            return api.blackMove(gameId, name, to, gotTicket).message
        }

        suspend fun getAllowedMoves(gameId: String, name: String): List<AllowedMoveResponse> {
            return api.getAllowedMoves(gameId, name)
        }

        suspend fun getMrXPosition(gameId: String, name: String): Int {
            return api.getMrXPosition(gameId, name)
        }

        suspend fun getAllowedDoubleMoves(gameId: String, name: String): List<MrXDoubleMoveResponse> {
            return api.getAllowedDoubleMoves(gameId, name)
        }

        suspend fun moveDouble(
            gameId: String,
            name: String,
            firstTo: Int,
            firstTicket: String,
            secondTo: Int,
            secondTicket: String
        ): MoveResponse {
            return api.moveDouble(gameId, name, firstTo, firstTicket, secondTo, secondTicket)
        }

        fun getPointPositions(context : Context): Map<Int, Pair<Int, Int>> {
            val json = context.assets.open("PointPositions.json").bufferedReader().use { it.readText() }
            val jsonObject = JsonParser.parseString(json).asJsonObject

            return jsonObject.entrySet().associate { entry ->
                val id = entry.key.toInt()
                val point = entry.value.asJsonArray[0].asJsonObject
                val x = point["x"].asInt
                val y = point["y"].asInt
                id to (x to y)
            }


        }

        suspend fun getMrXHistory(gameId: String): List<String> {
            println("Repository: getMrXHistory wird aufgerufen mit gameId=$gameId")
            return api.getMrXHistory(gameId)
        }


    }
