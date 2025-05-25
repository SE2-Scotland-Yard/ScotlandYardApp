package at.aau.serg.websocketbrokerdemo.repository

import androidx.compose.ui.graphics.vector.EmptyPath
import at.aau.serg.websocketbrokerdemo.data.api.GameApi
import at.aau.serg.websocketbrokerdemo.data.model.AllowedMoveResponse
import at.aau.serg.websocketbrokerdemo.data.model.MoveResponse
import at.aau.serg.websocketbrokerdemo.data.model.MrXDoubleMoveResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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

        fun getPointPositions(): Map<Int, Pair<Int, Int>> {
            //TODO: Implement loading in positions from json file, currently hard coded some for testing
            val map : Map<Int, Pair<Int, Int>> = mapOf(
                0 to Pair(0,0),
                1 to Pair(318, 78),
                2 to Pair(782, 42),
                3 to Pair(1082, 48),
                4 to Pair(1268, 36),
                5 to Pair(1996, 58),
                172 to Pair(1910,1542),
                173 to Pair(2170,1620),
                190 to Pair(290,1824),
                191 to Pair(438,1706)

            )
            return map
        }



    }
