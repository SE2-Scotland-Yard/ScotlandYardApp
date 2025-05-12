package at.aau.serg.websocketbrokerdemo.data.api

import androidx.annotation.IntegerRes
import at.aau.serg.websocketbrokerdemo.data.model.JoinResponse
import at.aau.serg.websocketbrokerdemo.data.model.LobbyState
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GameApi {

    @POST("api/game/move")
    suspend fun move(
        @Query("gameId") gameId: String,
        @Query("name")     name: String,
        @Query("to") to: Int,
        @Query("ticket")     ticket: String
    ): String

}