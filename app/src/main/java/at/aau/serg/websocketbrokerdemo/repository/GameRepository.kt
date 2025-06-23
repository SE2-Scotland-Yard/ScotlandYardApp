package at.aau.serg.websocketbrokerdemo.repository

import android.content.Context
import at.aau.serg.websocketbrokerdemo.data.api.GameApi
import at.aau.serg.websocketbrokerdemo.data.model.AllowedMoveResponse
import at.aau.serg.websocketbrokerdemo.data.model.LeaveGameRequest
import com.google.gson.JsonParser
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GameRepository(
    private val api: GameApi = Retrofit.Builder()
        .baseUrl("http://se2-demo.aau.at:53215/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GameApi::class.java)
) {
    val unknownError = "Unbekannter Fehler"

    suspend fun move(gameId: String, name: String, to: Int, gotTicket: String): String {
        return try {
            api.move(gameId, name, to, gotTicket).message
        } catch (e: Exception) {
            "Zug fehlgeschlagen: ${e.localizedMessage ?: unknownError}"
        }
    }

    suspend fun blackMove(gameId: String, name: String, to: Int, gotTicket: String): String {
        return try {
            api.blackMove(gameId, name, to, gotTicket).message
        } catch (e: Exception) {
            "Black Move fehlgeschlagen: ${e.localizedMessage ?: unknownError}"
        }
    }

    suspend fun getAllowedMoves(gameId: String, name: String): List<AllowedMoveResponse> {
        return try {
            api.getAllowedMoves(gameId, name)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMrXPosition(gameId: String, name: String): Int {
        return try {
            api.getMrXPosition(gameId, name)
        } catch (e: Exception) {
            -1
        }
    }

    suspend fun getAllowedDoubleMoves(gameId: String, name: String): List<AllowedMoveResponse> {
        return try {
            api.getAllowedDoubleMoves(gameId, name)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun moveDouble(gameId: String, name: String, to: Int, gotTicket: String): String {
        return try {
            api.moveDouble(gameId, name, to, gotTicket).message
        } catch (e: Exception) {
            "Doppelzug fehlgeschlagen: ${e.localizedMessage ?: unknownError}"
        }
    }

    fun getPointPositions(context: Context): Map<Int, Pair<Int, Int>> {
        return try {
            val json = context.assets.open("PointPositions.json").bufferedReader().use { it.readText() }
            val jsonObject = JsonParser.parseString(json).asJsonObject

            jsonObject.entrySet().associate { entry ->
                val id = entry.key.toInt()
                val point = entry.value.asJsonArray[0].asJsonObject
                val x = point["x"].asInt
                val y = point["y"].asInt
                id to (x to y)
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun getMrXHistory(gameId: String): List<String> {
        return try {
            println("Repository: getMrXHistory wird aufgerufen mit gameId=$gameId")
            api.getMrXHistory(gameId)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun leaveGame(gameId: String, playerId: String): Boolean {
        return try {
            val response = api.leaveGame(gameId, LeaveGameRequest(gameId, playerId))
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
