package at.aau.serg.websocketbrokerdemo.repository

import at.aau.serg.websocketbrokerdemo.data.api.GameApi
import at.aau.serg.websocketbrokerdemo.data.model.AllowedMoveResponse
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

    }
