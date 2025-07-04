package at.aau.serg.websocketbrokerdemo.data.api


import at.aau.serg.websocketbrokerdemo.data.model.AllowedMoveResponse
import at.aau.serg.websocketbrokerdemo.data.model.LeaveGameRequest

import at.aau.serg.websocketbrokerdemo.data.model.MoveResponse
import retrofit2.Response
import retrofit2.http.Body
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
        @Query("gotTicket")     gotTicket: String
    ): MoveResponse

    @GET("api/game/allowedMoves")
    suspend fun getAllowedMoves(
        @Query("gameId") gameId: String,
        @Query("name") name: String
    ): List<AllowedMoveResponse>


    @GET("api/game/mrXPosition")
    suspend fun getMrXPosition(
        @Query("gameId") gameId: String,
        @Query("name") name: String
    ): Int

    @GET("api/game/allowedDoubleMoves")
    suspend fun getAllowedDoubleMoves(
        @Query("gameId") gameId: String,
        @Query("name") name: String
    ): List<AllowedMoveResponse>

    @POST("api/game/moveDouble")
    suspend fun moveDouble(
        @Query("gameId") gameId: String,
        @Query("name") name: String,
        @Query("to") to: Int,
        @Query("gotTicket") gotTicket: String
    ): MoveResponse


    @POST("api/game/blackMove")
    suspend fun blackMove(
        @Query("gameId") gameId: String,
        @Query("name")     name: String,
        @Query("to") to: Int,
        @Query("gotTicket")     gotTicket: String
    ): MoveResponse

    @GET("/api/game/mrXhistory")
    suspend fun getMrXHistory(@Query("gameId") gameId: String): List<String>

    @POST("api/game/{gameId}/leave")
    suspend fun leaveGame(
        @Path("gameId") gameId: String,
        @Body request: LeaveGameRequest
    ): Response<Unit>


}